package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/** Static replacement effect that doubles damage dealt by creatures controlled by this effect's controller,
 * optionally restricted by an additional source predicate. */
public record DoubleDamageFromCreaturesEffect(PermanentPredicate additionalSourceFilter)
        implements SourceDamageMultiplyingEffect {

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    public DoubleDamageFromCreaturesEffect() {
        this(null);
    }

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        return additionalSourceFilter == null
                ? CREATURE_FILTER
                : new PermanentAllOfPredicate(List.of(CREATURE_FILTER, additionalSourceFilter));
    }
}
