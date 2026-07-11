package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot spell/ability effect: every creature the controller owns that matches
 * {@link #creatureFilter} gains "can be blocked only by blockers matching {@link #blockerPredicate}"
 * until end of turn. Used by Dread Charge ("black creatures you control can't be blocked this turn
 * except by black creatures").
 *
 * <p>The set of affected creatures is snapshotted when this effect resolves (CR 611): creatures that
 * become black or come under the controller later in the turn are not affected. The restriction is
 * stored transiently on each {@code Permanent} and consumed by
 * {@code GameQueryService.getBlockRestriction}.
 *
 * @param creatureFilter selects which of the controller's creatures gain the restriction; {@code null} = all
 * @param blockerPredicate the only blockers that may block the affected creatures
 * @param allowedBlockersDescription human-readable description of the allowed blockers (for logs/UI)
 */
public record GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect(
        PermanentPredicate creatureFilter,
        PermanentPredicate blockerPredicate,
        String allowedBlockersDescription
) implements CardEffect {
}
