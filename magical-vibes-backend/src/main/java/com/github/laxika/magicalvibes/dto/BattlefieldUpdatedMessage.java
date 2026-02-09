package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;

public record BattlefieldUpdatedMessage(MessageType type, List<List<Permanent>> battlefields) {
    public BattlefieldUpdatedMessage(List<List<Permanent>> battlefields) {
        this(MessageType.BATTLEFIELD_UPDATED, battlefields);
    }
}
