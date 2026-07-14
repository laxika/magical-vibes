package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Global static effect: while the source permanent is on the battlefield, any creature matching
 * {@code blockerPredicate} can't block any creature matching {@code attackerPredicate}
 * (e.g. Boldwyr Intimidator: "Cowards can't block Warriors."). Evaluated board-wide in
 * {@code GameQueryService.getBlockRestriction} — the restriction applies to every matching
 * blocker/attacker pair on any battlefield, not just the source's own creatures.
 *
 * @param blockerPredicate  which creatures are prevented from blocking (the "Cowards")
 * @param attackerPredicate which attackers they can't block (the "Warriors")
 * @param description       short human-readable phrase for the block-restriction message
 */
public record MatchingCreaturesCantBlockMatchingCreaturesEffect(PermanentPredicate blockerPredicate,
                                                                PermanentPredicate attackerPredicate,
                                                                String description) implements BlockingRestrictionEffect {

    @Override
    public PermanentPredicate globalCantBlockBlockerMatcher() {
        return blockerPredicate;
    }

    @Override
    public PermanentPredicate globalCantBlockAttackerMatcher() {
        return attackerPredicate;
    }

    @Override
    public String globalCantBlockDescription() {
        return description;
    }
}
