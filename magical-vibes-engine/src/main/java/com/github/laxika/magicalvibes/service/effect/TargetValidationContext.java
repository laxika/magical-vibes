package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.UUID;

/**
 * What a target is validated against. {@code eventValue} is the value of the event a triggered ability triggered on
 * (the stack entry's as the ability resolves), which a target restricted by it is judged against
 * ({@code CardManaValueEqualsEventValuePredicate}); {@code null} for a spell, an activated ability and wherever no
 * event is known.
 */
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
        UUID defendingPlayerId,
        UUID triggeringPermanentId,
        UUID triggeringPlayerId,
        Integer eventValue
) {
    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   boolean deferCostDerivedXValueChecks, UUID sourcePermanentId,
                                   Integer sourcePowerAtTrigger, UUID defendingPlayerId,
                                   UUID triggeringPermanentId, UUID triggeringPlayerId) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId, sourcePermanentSnapshot,
                deferCostDerivedXValueChecks, sourcePermanentId, sourcePowerAtTrigger, defendingPlayerId,
                triggeringPermanentId, triggeringPlayerId, null);
    }

    /** This context with the value of the event the triggered ability triggered on. */
    public TargetValidationContext withEventValue(Integer eventValue) {
        return new TargetValidationContext(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, deferCostDerivedXValueChecks, sourcePermanentId, sourcePowerAtTrigger,
                defendingPlayerId, triggeringPermanentId, triggeringPlayerId, eventValue);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   boolean deferCostDerivedXValueChecks, UUID sourcePermanentId,
                                   Integer sourcePowerAtTrigger, UUID defendingPlayerId,
                                   UUID triggeringPermanentId) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId, sourcePermanentSnapshot,
                deferCostDerivedXValueChecks, sourcePermanentId, sourcePowerAtTrigger, defendingPlayerId,
                triggeringPermanentId, (UUID) null);
    }

    /** This context with the player the triggered ability's event was about ("that player controls"). */
    public TargetValidationContext withTriggeringPlayerId(UUID triggeringPlayerId) {
        return new TargetValidationContext(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, deferCostDerivedXValueChecks, sourcePermanentId, sourcePowerAtTrigger,
                defendingPlayerId, triggeringPermanentId, triggeringPlayerId, eventValue);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard) {
        this(gameData, targetId, targetZone, sourceCard, 0, null, null, false, null, null, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard, int xValue) {
        this(gameData, targetId, targetZone, sourceCard, xValue, null, null, false, null, null, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, false, null, null, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   boolean deferCostDerivedXValueChecks) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, deferCostDerivedXValueChecks, null, null, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   UUID sourcePermanentId, Integer sourcePowerAtTrigger) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, false, sourcePermanentId, sourcePowerAtTrigger, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   boolean deferCostDerivedXValueChecks, UUID sourcePermanentId,
                                   Integer sourcePowerAtTrigger) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, deferCostDerivedXValueChecks, sourcePermanentId,
                sourcePowerAtTrigger, null, null);
    }

    public TargetValidationContext(GameData gameData, UUID targetId, Zone targetZone, Card sourceCard,
                                   int xValue, UUID sourceControllerId, Permanent sourcePermanentSnapshot,
                                   UUID sourcePermanentId, Integer sourcePowerAtTrigger,
                                   UUID defendingPlayerId) {
        this(gameData, targetId, targetZone, sourceCard, xValue, sourceControllerId,
                sourcePermanentSnapshot, false, sourcePermanentId, sourcePowerAtTrigger,
                defendingPlayerId, null);
    }
}

