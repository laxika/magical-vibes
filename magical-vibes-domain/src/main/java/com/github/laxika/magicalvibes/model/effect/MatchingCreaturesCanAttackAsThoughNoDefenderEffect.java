package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect on a permanent (any controller): every creature matching {@code matcher}
 * can attack as though it didn't have defender. Global analogue of the self-only
 * {@link CanAttackAsThoughNoDefenderEffect}, checked in
 * {@code GameQueryService.canAttackDespiteDefender}. Used by Rolling Stones (Wall creatures).
 */
public record MatchingCreaturesCanAttackAsThoughNoDefenderEffect(PermanentPredicate matcher)
        implements NoDefenderAttackPermissionEffect {

    @Override
    public PermanentPredicate noDefenderAttackMatcher() {
        return matcher;
    }
}
