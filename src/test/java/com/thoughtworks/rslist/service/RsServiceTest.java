package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;
  LocalDateTime localDateTime;
  Vote vote;
  Trade trade;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
    trade = Trade.builder().amount(100).rankNum(1).rsEventId(3).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyWord("keyWord")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> rsService.vote(vote, 1)
        );
  }

  @Test
  void shouldBuySuccess() {
    // given

    UserDto userDto =
            UserDto.builder()
                    .voteNum(5)
                    .phone("18888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("xiaoli")
                    .id(2)
                    .build();

    RsEventDto rsEvent1 = RsEventDto.builder()
            .id(1)
            .eventName("购买前的事件")
            .keyWord("之前")
            .voteNum(5)
            .user(userDto)
            .build();

    RsEventDto rsEvent2 = RsEventDto.builder()
            .id(2)
            .eventName("购买前的事件2")
            .keyWord("之前")
            .voteNum(5)
            .user(userDto)
            .build();

    RsEventDto rsEvent3 = RsEventDto.builder()
            .id(3)
            .eventName("购买的事件")
            .keyWord("之后")
            .voteNum(5)
            .user(userDto)
            .build();

    TradeDto tradeDto1 = TradeDto.builder()
            .amount(50)
            .rankNum(1)
            .rsEventDto(rsEvent1)
            .build();

    TradeDto tradeDto2 = TradeDto.builder()
            .amount(60)
            .rankNum(1)
            .rsEventDto(rsEvent2)
            .build();

    when(tradeRepository.findAllByRankNumOrderByAmountDesc(anyInt())).thenReturn(Arrays.asList(tradeDto2, tradeDto1));
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEvent3));

    // when
    rsService.buy(trade, 3);
    // then
    verify(tradeRepository).save(TradeDto.builder()
            .amount(100)
            .rankNum(1)
            .rsEventDto(rsEvent3)
            .build());
    verify(rsEventRepository).deleteById(2);

  }
}
