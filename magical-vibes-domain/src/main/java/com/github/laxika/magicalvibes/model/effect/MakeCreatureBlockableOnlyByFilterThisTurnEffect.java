package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot ability effect: a creature can be blocked only by blockers matching
 * {@link #blockerPredicate} until end of turn. Used by Joven's Tools ("target creature can't be
 * blocked this turn except by Walls") and by self-targeting trigger payoffs such as Rhonas's
 * Stalwart ("can't be blocked by creatures with power 2 or less this turn" ≡ only by power 3+).
 *
 * <p>The single-creature counterpart of {@link GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect}:
 * the restriction is added to the creature's {@code Permanent.blockRestrictionsUntilEndOfTurn} and
 * consumed by {@code GameQueryService.getBlockRestriction}, cleared by {@code resetModifiers}.
 *
 * @param blockerPredicate the only blockers that may block the creature
 * @param allowedBlockersDescription human-readable description of the allowed blockers (for logs/UI)
 * @param selfTargeting when true, applies to the stack entry's source permanent (no target); when
 *        false, applies to the targeted permanent (benign CREATURE/PERMANENT target)
 */
public record MakeCreatureBlockableOnlyByFilterThisTurnEffect(
        PermanentPredicate blockerPredicate,
        String allowedBlockersDescription,
        boolean selfTargeting
) implements CardEffect {

    public MakeCreatureBlockableOnlyByFilterThisTurnEffect(
            PermanentPredicate blockerPredicate,
            String allowedBlockersDescription) {
        this(blockerPredicate, allowedBlockersDescription, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return selfTargeting
                ? new TargetSpec(TargetCategory.NONE, false, null, true, 1)
                : TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
