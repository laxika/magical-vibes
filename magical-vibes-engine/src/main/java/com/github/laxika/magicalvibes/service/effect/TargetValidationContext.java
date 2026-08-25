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
        boolean deferCostDerivedXValueChecks,
        UUID sourcePermanentId,
        Integer sourcePowerAtTrigger,
        UUID defendingPlayerId
) {
    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard) {
        this(gameData, targetId, targetZone, sourceCard, 0, null, null, false, null, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard, int xValue) {
        this(gameData, targetId, targetZone, sourceCard, xValue, null, null, false, null, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, false, null, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   boolean deferCostDerivedXValueChecks) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, deferCostDerivedXValueChecks, null, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   UUID sourcePermanentId, Integer sourcePowerAtTrigger) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, false, sourcePermanentId, sourcePowerAtTrigger, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   boolean deferCostDerivedXValueChecks, UUID sourcePermanentId,
                                   Integer sourcePowerAtTrigger) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, deferCostDerivedXValueChecks, sourcePermanentId,
                sourcePowerAtTrigger, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   UUID sourcePermanentId, Integer sourcePowerAtTrigger,
                                   UUID defendingPlayerId) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, false, sourcePermanentId, sourcePowerAtTrigger,
                defendingPlayerId);
    }
}

