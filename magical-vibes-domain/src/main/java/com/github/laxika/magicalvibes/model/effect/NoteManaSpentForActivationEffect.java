package com.github.laxika.magicalvibes.model.effect;

/**
 * Notes on the source permanent the type and amount of mana that was spent to pay the activation
 * cost of the ability being resolved (Ice Cauldron). The note replaces any previous one and is read
 * back by {@link AddNotedManaForLastExiledCardEffect}.
 */
public record NoteManaSpentForActivationEffect() implements CardEffect {
}
