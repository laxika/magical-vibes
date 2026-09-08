package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Registers Mystic Reflection's one-shot replacement for the next creature or planeswalker entry
 * event this turn.
 */
public record RegisterMysticReflectionEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.creature(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))
                )));
    }
}
