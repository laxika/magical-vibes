package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Adds one mana of the type produced by the permanent whose mana ability caused this trigger.
 * When {@code requiredColor} is non-null, the effect only applies when that color was produced
 * and adds one mana of that color.
 */
public record AddManaOfTypeProducedByTappedPermanentEffect(ManaColor requiredColor)
        implements ManaProducingEffect, ProducedManaColorAwareEffect {

    public AddManaOfTypeProducedByTappedPermanentEffect() {
        this(null);
    }

    @Override
    public CardEffect withProducedManaColors(Set<ManaColor> producedManaColors) {
        return this;
    }
}
