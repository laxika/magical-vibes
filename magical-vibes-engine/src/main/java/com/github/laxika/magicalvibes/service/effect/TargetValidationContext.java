package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.UUID;

public record TargetValidationContext(
        GameData gameData,
        UUID targetId,
        Zone targetZone,
        Card sourceCard,
        int xValue,
        UUID sourceControllerId,
        Permanent sourcePermanentSnapshot,
        boolean deferCostDerivedXValueChecks
) {
    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard) {
        this(gameData, targetId, targetZone, sourceCard, 0, null, null, false);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard, int xValue) {
        this(gameData, targetId, targetZone, sourceCard, xValue, null, null, false);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, false);
    }
}

