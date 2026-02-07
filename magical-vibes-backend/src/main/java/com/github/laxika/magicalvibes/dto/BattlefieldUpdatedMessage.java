package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MessageType;

import java.util.List;

public record BattlefieldUpdatedMessage(MessageType type, List<List<Card>> battlefields) {
    public BattlefieldUpdatedMessage(List<List<Card>> battlefields) {
        this(MessageType.BATTLEFIELD_UPDATED, battlefields);
    }
}
