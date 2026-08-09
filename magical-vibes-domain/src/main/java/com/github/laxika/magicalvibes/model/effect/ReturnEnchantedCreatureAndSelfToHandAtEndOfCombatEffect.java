package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the source Aura and the creature it enchants to be returned to their owners' hands at
 * end of combat. Used by Contempt's attack trigger.
 */
public record ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffect() implements CardEffect {
}
