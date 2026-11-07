package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.UUID;

/**
 * Context for predicate evaluation.
 *
 * @param sourcePermanentSnapshot last known information for the ability's source permanent, used by
 *        source-relative predicates when the source has left the battlefield (CR 608.2b: "If the
 *        source of an ability has left the zone it was in, its last known information is used").
 *        Set on resolution-time target re-checks; {@code null} elsewhere.
 * @param activatedAbilitySource true when evaluating for an activated ability on the stack; the
 *        source's last known combat relationships are then not used once it has left the battlefield
 *        (that look-back is reserved for triggered abilities such as death triggers).
 * @param triggeringPermanentSnapshot the permanent the resolving triggered ability's event was about,
 *        as it last existed on the battlefield (the stack entry's triggering permanent, CR 603.10a), for
 *        predicates that compare with "it" (Martyr's Bond's "a permanent that shares a card type with
 *        it"); {@code null} elsewhere.
 * @param triggeringPlayerId the player the triggered ability's event was about (the stack entry's triggering player:
 *        the player dealt damage), for {@link PermanentControlledByTriggeringPlayerPredicate}'s "that player controls";
 *        {@code null} elsewhere.
 * @param triggeringSpellTargetIds what the spell that triggered the resolving ability targets (its target and declared
 *        targets), for {@link PermanentIsTargetOfTriggeringSpellPredicate}'s "those permanents" (Dack Fayden's emblem);
 *        empty elsewhere.
 */
public record FilterContext(
        GameData gameData,
        UUID sourceCardId,
        UUID sourceControllerId,
        Integer xValue,
        Permanent sourcePermanentSnapshot,
        UUID sourcePermanentId,
        boolean madness,
        UUID defendingPlayerId,
        UUID triggeringPermanentId,
        UUID targetCardId,
        UUID triggeringPermanentControllerId,
        UUID enteringControllerId,
        Permanent attachedPermanentSnapshot,
        boolean activatedAbilitySource,
        Permanent triggeringPermanentSnapshot,
        UUID triggeringPlayerId,
        List<UUID> triggeringSpellTargetIds
) {
    public FilterContext {
        triggeringSpellTargetIds = triggeringSpellTargetIds == null ? List.of() : List.copyOf(triggeringSpellTargetIds);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness, UUID defendingPlayerId, UUID triggeringPermanentId,
                         UUID targetCardId, UUID triggeringPermanentControllerId,
                         UUID enteringControllerId, Permanent attachedPermanentSnapshot,
                         boolean activatedAbilitySource, Permanent triggeringPermanentSnapshot,
                         UUID triggeringPlayerId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId,
                targetCardId, triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot,
                activatedAbilitySource, triggeringPermanentSnapshot, triggeringPlayerId, List.of());
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness, UUID defendingPlayerId, UUID triggeringPermanentId,
                         UUID targetCardId, UUID triggeringPermanentControllerId,
                         UUID enteringControllerId, Permanent attachedPermanentSnapshot,
                         boolean activatedAbilitySource, Permanent triggeringPermanentSnapshot) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId,
                targetCardId, triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot,
                activatedAbilitySource, triggeringPermanentSnapshot, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness, UUID defendingPlayerId, UUID triggeringPermanentId,
                         UUID targetCardId, UUID triggeringPermanentControllerId,
                         UUID enteringControllerId, Permanent attachedPermanentSnapshot,
                         boolean activatedAbilitySource) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId,
                targetCardId, triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot,
                activatedAbilitySource, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness, UUID defendingPlayerId, UUID triggeringPermanentId,
                         UUID targetCardId, UUID triggeringPermanentControllerId,
                         UUID enteringControllerId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId,
                targetCardId, triggeringPermanentControllerId, enteringControllerId, null, false);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness, UUID defendingPlayerId, UUID triggeringPermanentId,
                         UUID targetCardId, UUID triggeringPermanentControllerId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId,
                targetCardId, triggeringPermanentControllerId, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness, UUID defendingPlayerId, UUID triggeringPermanentId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, null, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness, UUID defendingPlayerId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, null, null, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                null, false, null, null, null, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, false, null, null, null, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, null, null, null, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         UUID defendingPlayerId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, false, defendingPlayerId, null, null, null);
    }

    public static FilterContext empty() {
        return new FilterContext(null, null, null, null, null, null, false, null, null, null, null);
    }

    public static FilterContext of(GameData gameData) {
        return new FilterContext(gameData, null, null, null, null, null, false, null, null, null, null);
    }

    public FilterContext withSourceCardId(UUID sourceCardId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withSourceControllerId(UUID sourceControllerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withXValue(int xValue) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withSourcePermanentSnapshot(Permanent sourcePermanentSnapshot) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withSourcePermanentId(UUID sourcePermanentId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withMadness(boolean madness) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withDefendingPlayerId(UUID defendingPlayerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withTriggeringPermanentId(UUID triggeringPermanentId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withTriggeringPermanentControllerId(UUID triggeringPermanentControllerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withTargetCardId(UUID targetCardId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withEnteringControllerId(UUID enteringControllerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    /** Last-known state of the creature the source Aura or Equipment was attached to when it triggered. */
    public FilterContext withAttachedPermanentSnapshot(Permanent attachedPermanentSnapshot) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withActivatedAbilitySource(boolean activatedAbilitySource) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withTriggeringPermanentSnapshot(Permanent triggeringPermanentSnapshot) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    public FilterContext withTriggeringPlayerId(UUID triggeringPlayerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }

    /** This context with {@code triggeringSpellTargetIds} as what the triggering spell targets. */
    public FilterContext withTriggeringSpellTargetIds(List<UUID> triggeringSpellTargetIds) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId, triggeringPermanentId, targetCardId,
                triggeringPermanentControllerId, enteringControllerId, attachedPermanentSnapshot, activatedAbilitySource,
                triggeringPermanentSnapshot, triggeringPlayerId, triggeringSpellTargetIds);
    }
}
