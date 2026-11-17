package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.PendingMysticReflection;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Registers Mystic Reflection's one-shot replacement effect (CR 614.1c) for the next creature or planeswalker entry
 * event this turn. The resolution makes a floating effect on the spell's controller whose payload is this record
 * with the chosen creature; it ends at cleanup ("this turn") or as the entry applies it.
 *
 * @param reflection the replacement the resolution recorded; {@code null} on the card's own copy
 */
public record RegisterMysticReflectionEffect(PendingMysticReflection reflection) implements CardEffect {

    public RegisterMysticReflectionEffect() {
        this(null);
    }


    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.creature(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))
                )));
    }
}
