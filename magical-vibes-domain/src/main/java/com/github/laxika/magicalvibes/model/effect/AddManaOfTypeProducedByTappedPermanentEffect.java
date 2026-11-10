package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Adds one mana of the type produced by the permanent whose mana ability caused this trigger (the stack entry's
 * produced mana color, {@code StackEntry.getProducedManaColors()}).
 * When {@code requiredColor} is non-null, the effect only applies when that color was produced
 * and adds one mana of that color. The mana goes to the player who tapped the permanent (the entry's triggering
 * player); when the permanent produced several types, that player chooses one.
 */
public record AddManaOfTypeProducedByTappedPermanentEffect(ManaColor requiredColor)
        implements ManaProducingEffect, TriggeredManaAbilityEffect {

    public AddManaOfTypeProducedByTappedPermanentEffect() {
        this(null);
    }

    @Override
    public boolean readsProducedMana() {
        return true;
    }
}
