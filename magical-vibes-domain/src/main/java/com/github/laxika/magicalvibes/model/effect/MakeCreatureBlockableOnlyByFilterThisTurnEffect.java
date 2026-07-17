package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot targeted ability effect: the target creature can be blocked only by blockers matching
 * {@link #blockerPredicate} until end of turn. Used by Joven's Tools ("target creature can't be
 * blocked this turn except by Walls").
 *
 * <p>The single-creature, targeted counterpart of
 * {@link GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect}: the restriction is added to the target
 * {@code Permanent.blockRestrictionsUntilEndOfTurn} and consumed by
 * {@code GameQueryService.getBlockRestriction}, cleared by {@code resetModifiers}.
 *
 * @param blockerPredicate the only blockers that may block the target creature
 * @param allowedBlockersDescription human-readable description of the allowed blockers (for logs/UI)
 */
public record MakeCreatureBlockableOnlyByFilterThisTurnEffect(
        PermanentPredicate blockerPredicate,
        String allowedBlockersDescription
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
