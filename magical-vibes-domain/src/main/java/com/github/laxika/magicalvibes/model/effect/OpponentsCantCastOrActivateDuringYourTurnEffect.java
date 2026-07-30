package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: during the source controller's turn, their opponents can't cast spells or
 * activate abilities of artifact, creature, or enchantment permanents (mana abilities included).
 * Used by Grand Abolisher (M12).
 */
public record OpponentsCantCastOrActivateDuringYourTurnEffect() implements CardEffect {
}
