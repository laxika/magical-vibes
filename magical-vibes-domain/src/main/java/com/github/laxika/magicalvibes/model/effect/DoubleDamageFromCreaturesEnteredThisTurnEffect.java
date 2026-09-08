package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Static effect that doubles damage dealt by creatures controlled by this permanent's controller
 * that entered the battlefield this turn.
 */
public record DoubleDamageFromCreaturesEnteredThisTurnEffect() implements SourceDamageMultiplyingEffect {

    private static final PermanentPredicate SOURCE_FILTER = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentEnteredBattlefieldThisTurnPredicate()));

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        return SOURCE_FILTER;
    }
}
