package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

/**
 * Puts a +1/+1 counter on the target creature and creates the token if that creature shares a color with the mana the
 * tap for mana that triggered the ability produced (the stack entry's produced mana colors,
 * {@code StackEntry.getProducedManaColors()}).
 */
public record PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(CreateTokenEffect token)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentControlledBySourceControllerPredicate());
    }
}
