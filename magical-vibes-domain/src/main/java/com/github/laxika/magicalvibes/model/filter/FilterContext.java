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
        UUID sourcePermanentId
) {
    public FilterContext(GameData gameData, UUID sourceCardId, UUID sourceControllerId,
                         Integer xValue, Permanent sourcePermanentSnapshot) {
        this(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot, null);
    }

    public static FilterContext empty() {
        return new FilterContext(null, null, null, null, null, null);
    }

    public static FilterContext of(GameData gameData) {
        return new FilterContext(gameData, null, null, null, null, null);
    }

    public FilterContext withSourceCardId(UUID sourceCardId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId);
    }

    public FilterContext withSourceControllerId(UUID sourceControllerId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId);
    }

    public FilterContext withXValue(int xValue) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId);
    }

    public FilterContext withSourcePermanentSnapshot(Permanent sourcePermanentSnapshot) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId);
    }

    public FilterContext withSourcePermanentId(UUID sourcePermanentId) {
        return new FilterContext(gameData, sourceCardId, sourceControllerId, xValue, sourcePermanentSnapshot,
                sourcePermanentId);
    }
}
