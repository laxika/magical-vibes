package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Causes permanents matching the given subject to lose all abilities except mana abilities.
 * This is a continuous layer-6 effect used by cards such as Blood Sun.
 *
 * @param subject  which permanents are affected
 * @param filter optional additional permanent filter
 */
public record LosesAllNonManaAbilitiesEffect(PermanentSubject subject, PermanentPredicate filter)
        implements CardEffect {

    public LosesAllNonManaAbilitiesEffect(PermanentSubject subject) {
        this(subject, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
