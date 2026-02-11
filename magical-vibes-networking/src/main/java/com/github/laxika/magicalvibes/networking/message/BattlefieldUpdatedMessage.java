package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.networking.model.PermanentView;

import java.util.List;

public record BattlefieldUpdatedMessage(MessageType type, List<List<PermanentView>> battlefields) {
    public BattlefieldUpdatedMessage(List<List<PermanentView>> battlefields) {
        this(MessageType.BATTLEFIELD_UPDATED, battlefields);
    }
}
