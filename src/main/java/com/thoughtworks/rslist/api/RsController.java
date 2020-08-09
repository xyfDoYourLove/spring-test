package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.exception.Error;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.service.RsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@Validated
public class RsController {
  @Autowired RsEventRepository rsEventRepository;
  @Autowired UserRepository userRepository;
  @Autowired RsService rsService;
  @Autowired TradeRepository tradeRepository;

  @GetMapping("/rs/list")
  public ResponseEntity<List<RsEvent>> getRsEventListBetween(
      @RequestParam(required = false) Integer start, @RequestParam(required = false) Integer end) {

    AtomicInteger rank = new AtomicInteger();
    Map<Integer, RsEvent> allRsEvents =
        rsEventRepository.findAll().stream()
            .sorted(Comparator.comparing(RsEventDto::getVoteNum).reversed())
            .map(
                item ->
                    RsEvent.builder()
                        .id(item.getId())
                        .eventName(item.getEventName())
                        .keyWord(item.getKeyWord())
                        .userId(item.getId())
                        .voteNum(item.getVoteNum())
                        .nowRank(rank.getAndIncrement())
                        .build())
            .collect(Collectors.toMap(RsEvent::getId, item -> item));

    Map<Integer, RsEvent> idTradeRsEventsMap = new HashMap<>();
    Map<Integer, RsEvent> rankTradeRsEventsMap = new HashMap<>();
    List<RsEvent> noTradeRsEvents;

    // 根据购买记录，对list的排序重新进行调整
    for (int i = 0; i < allRsEvents.size(); i++) {
      List<TradeDto> tradeRecord = tradeRepository.findAllByRankNumOrderByAmountDesc(i + 1);
      if (!tradeRecord.isEmpty()) {
        RsEvent rsEvent = allRsEvents.get(tradeRecord.get(0).getRsEventDto().getId());
        rsEvent.setNowRank(tradeRecord.get(0).getRankNum());
        idTradeRsEventsMap.put(rsEvent.getId(), rsEvent);
        rankTradeRsEventsMap.put(rsEvent.getNowRank(), rsEvent);
      }
    }

    noTradeRsEvents = rsEventRepository.findAll().stream()
            .filter(item -> idTradeRsEventsMap.get(item.getId()) != null)
            .sorted(Comparator.comparing(RsEventDto::getVoteNum).reversed())
            .map(
                item ->
                    RsEvent.builder()
                        .id(item.getId())
                        .eventName(item.getEventName())
                        .keyWord(item.getKeyWord())
                        .userId(item.getId())
                        .voteNum(item.getVoteNum())
                        .nowRank(0)
                        .build())
            .collect(Collectors.toList());

    List<RsEvent> sortedRsEvents = new ArrayList<>();
    if (!noTradeRsEvents.isEmpty()) {
      for (int i = 0, j = 0; i < allRsEvents.size(); i++) {
        if (rankTradeRsEventsMap.get(i + 1) != null) {
          sortedRsEvents.add(rankTradeRsEventsMap.get(i + 1));
        }else {
          RsEvent rsEvent = noTradeRsEvents.get(j);
          rsEvent.setNowRank(i + 1);
          sortedRsEvents.add(rsEvent);
          j++;
        }
      }
    }

    if (start == null || end == null || noTradeRsEvents.isEmpty()) {
      return ResponseEntity.ok(sortedRsEvents);
    }
    return ResponseEntity.ok(sortedRsEvents.subList(start - 1, end));
  }

  @GetMapping("/rs/{index}")
  public ResponseEntity<RsEvent> getRsEvent(@PathVariable int index) {
    List<RsEvent> rsEvents =
        rsEventRepository.findAll().stream()
            .map(
                item ->
                    RsEvent.builder()
                        .eventName(item.getEventName())
                        .keyWord(item.getKeyWord())
                        .userId(item.getId())
                        .voteNum(item.getVoteNum())
                        .build())
            .collect(Collectors.toList());
    if (index < 1 || index > rsEvents.size()) {
      throw new RequestNotValidException("invalid index");
    }
    return ResponseEntity.ok(rsEvents.get(index - 1));
  }

  @PostMapping("/rs/event")
  public ResponseEntity addRsEvent(@RequestBody @Valid RsEvent rsEvent) {
    Optional<UserDto> userDto = userRepository.findById(rsEvent.getUserId());
    if (!userDto.isPresent()) {
      return ResponseEntity.badRequest().build();
    }
    RsEventDto build =
        RsEventDto.builder()
            .keyWord(rsEvent.getKeyWord())
            .eventName(rsEvent.getEventName())
            .voteNum(0)
            .user(userDto.get())
            .build();
    rsEventRepository.save(build);
    return ResponseEntity.created(null).build();
  }

  @PostMapping("/rs/vote/{id}")
  public ResponseEntity vote(@PathVariable int id, @RequestBody Vote vote) {
    rsService.vote(vote, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/rs/buy/{id}")
  public ResponseEntity buy(@PathVariable int id, @RequestBody @Valid Trade trade){
    Boolean isSuccess = rsService.buy(trade, id);
    if (isSuccess) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.badRequest().build();
  }


  @ExceptionHandler(RequestNotValidException.class)
  public ResponseEntity<Error> handleRequestErrorHandler(RequestNotValidException e) {
    Error error = new Error();
    error.setError(e.getMessage());
    return ResponseEntity.badRequest().body(error);
  }
}
