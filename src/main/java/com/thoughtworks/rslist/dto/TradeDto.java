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
@Table(name = "trade")
public class TradeDto {
    @Id
    @GeneratedValue
    private int id;

    private int amount;

    private int rankNum;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "tradeDto")
    private RsEventDto rsEventDto;
}
