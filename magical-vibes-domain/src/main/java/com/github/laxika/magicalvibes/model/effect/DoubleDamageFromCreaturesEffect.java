package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Static replacement effect that doubles damage dealt by creatures controlled by this effect's controller. */
public record DoubleDamageFromCreaturesEffect() implements SourceDamageMultiplyingEffect {

    private static final PermanentPredicate SOURCE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        return SOURCE_FILTER;
    }
}
