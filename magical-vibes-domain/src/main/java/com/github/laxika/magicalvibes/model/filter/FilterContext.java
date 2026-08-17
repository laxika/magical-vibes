package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Context for predicate evaluation.
 *
 * @param sourcePermanentSnapshot last known information for the ability's source permanent, used by
 *        source-relative predicates when the source has left the battlefield (CR 608.2b: "If the
 *        source of an ability has left the zone it was in, its last known information is used").
 *        Set on resolution-time target re-checks; {@code null} elsewhere.
 */
public record FilterContext(
        GameData gameData,
        UUID sourceCardId,
        UUID sourceControllerId,
        Integer xValue,
        Permanent sourcePermanentSnapshot,
        UUID sourcePermanentId,
        boolean madness,
        UUID defendingPlayerId
) {
    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot, null, false, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, false, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         boolean madness) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, null);
    }

    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot, UUID sourcePermanentId,
                         UUID defendingPlayerId) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, false, defendingPlayerId);
    }

    public static FilterContext empty() {
        return new FilterContext(null, null, null, null, null, null, false, null);
    }

    public static FilterContext of(GameData gameData) {
        return new FilterContext(gameData, null, null, null, null, null, false, null);
    }

    public FilterContext withSourceCardId(UUID sourceCardId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId);
    }

    public FilterContext withSourceControllerId(UUID sourceControllerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId);
    }

    public FilterContext withXValue(int xValue) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId);
    }

    public FilterContext withSourcePermanentSnapshot(Permanent sourcePermanentSnapshot) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId);
    }

    public FilterContext withSourcePermanentId(UUID sourcePermanentId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId);
    }

    public FilterContext withMadness(boolean madness) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId);
    }

    public FilterContext withDefendingPlayerId(UUID defendingPlayerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId, madness, defendingPlayerId);
    }
}
