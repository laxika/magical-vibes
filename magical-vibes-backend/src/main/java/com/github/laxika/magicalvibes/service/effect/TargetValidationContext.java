package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.UUID;

public record TargetValidationContext(
        GameData gameData,
        UUID targetId,
        Zone targetZone,
        Card sourceCard,
        int xValue
) {
    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard) {
        this(gameData, targetId, targetZone, sourceCard, 0);
    }
}

