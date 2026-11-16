package com.github.laxika.magicalvibes.model.filter;

import java.util.UUID;

/**
 * Matches a permanent that the source object {@code sourceId} dealt damage to this turn: a permanent by its permanent
 * id, a spell by its card id. Evaluated against {@code GameData.damageDealtToPermanentsBySourceThisTurn}, which keeps
 * a permanent's damage after it leaves the battlefield, so a delayed triggered ability judges a creature that dies as
 * it last existed (CR 603.10a). An engine-made filter: the handler that creates a delayed triggered ability names the
 * source ("whenever a creature dealt damage this way dies this turn", Reckless Blaze; "dealt damage by that creature",
 * Touch of Moonglove).
 */
public record PermanentDealtDamageThisTurnBySourcePredicate(UUID sourceId) implements PermanentPredicate {
}
