package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;

import java.util.List;

public record LifeUpdatedMessage(MessageType type, List<Integer> lifeTotals) {
    public LifeUpdatedMessage(List<Integer> lifeTotals) {
        this(MessageType.LIFE_UPDATED, lifeTotals);
    }
}
