package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger for Gift of Immortality: return the enchanted creature under its owner's control,
 * then schedule the source Aura to return attached to that returned permanent at the next end step.
 * "That card" is the stack entry's triggering card ({@code StackEntry.getTriggeringCards()}), with the graveyard incarnation the enchanted-death
 * collector recorded for it (CR 400.7).
 */
public record ReturnEnchantedCreatureAndReattachAuraOnDeathEffect() implements CardEffect {
}
