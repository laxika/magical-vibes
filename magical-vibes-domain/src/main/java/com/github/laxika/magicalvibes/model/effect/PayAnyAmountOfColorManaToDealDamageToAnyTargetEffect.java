package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/** Pays any amount of mana of one color, then creates a reflexive damage ability. */
public record PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect(ManaColor color) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
