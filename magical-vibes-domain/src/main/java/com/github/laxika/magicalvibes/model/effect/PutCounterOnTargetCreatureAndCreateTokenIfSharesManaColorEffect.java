package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

import java.util.Set;

/** Puts a +1/+1 counter on the target creature and conditionally creates a token. */
public record PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(
        CreateTokenEffect token,
        Set<ManaColor> producedManaColors
) implements ProducedManaColorAwareEffect {

    public PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(CreateTokenEffect token) {
        this(token, Set.of());
    }

    public PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect {
        producedManaColors = Set.copyOf(producedManaColors);
    }

    @Override
    public CardEffect withProducedManaColors(Set<ManaColor> producedManaColors) {
        return new PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(token, producedManaColors);
    }

    @Override
    public boolean requiresTargetChoiceAfterProducedMana() {
        return true;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentControlledBySourceControllerPredicate());
    }
}
