package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Static effect that doubles all damage dealt by this permanent. */
public record DoubleDamageFromSelfEffect() implements SourceDamageMultiplyingEffect {

    private static final PermanentPredicate SOURCE_FILTER = new PermanentIsSourcePermanentPredicate();

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        return SOURCE_FILTER;
    }
}
