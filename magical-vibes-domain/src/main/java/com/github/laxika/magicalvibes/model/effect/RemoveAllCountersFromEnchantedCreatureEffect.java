package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes every counter of every kind from the creature enchanted by the source Aura, without
 * targeting it (e.g. Enchanted River's Grasp's "remove all counters from enchanted creature").
 * The host is read from the Aura's attachment when the effect resolves, so hexproof or protection
 * granted to the creature does not stop the removal.
 */
public record RemoveAllCountersFromEnchantedCreatureEffect() implements CardEffect {
}
