package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record LifeUpdatedMessage(MessageType type, List<Integer> lifeTotals) {
    public LifeUpdatedMessage(List<Integer> lifeTotals) {
        this(MessageType.LIFE_UPDATED, lifeTotals);
    }
}
