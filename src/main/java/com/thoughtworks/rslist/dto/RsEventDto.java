package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rs_event")
public class RsEventDto {
  @Id @GeneratedValue private int id;
  private String eventName;
  private String keyWord;
  private int voteNum;
  @ManyToOne private UserDto user;
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "trade_id")
  private TradeDto tradeDto;
}
