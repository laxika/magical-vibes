package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/** An effect whose resolution depends on the color chosen for a mana ability. */
public interface ProducedManaColorAwareEffect extends CardEffect {

    CardEffect withProducedManaColors(Set<ManaColor> producedManaColors);

    default boolean requiresTargetChoiceAfterProducedMana() {
        return false;
    }
}
