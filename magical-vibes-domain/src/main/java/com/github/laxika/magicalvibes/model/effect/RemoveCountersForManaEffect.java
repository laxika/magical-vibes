package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Mana ability: "Remove any number of {@code counterType} counters from this permanent:
 * Add one {@code color} mana for each counter removed this way." (the storage-land cycle —
 * Bottomless Vault, Dwarven Hold, Hollow Trees, Icatian Store, Sand Silos).
 *
 * <p>Resolved in {@code ActivatedAbilityExecutionService}'s mana-ability path: it prompts the
 * controller for how many counters (0..the count present) to remove, then removes that many and
 * adds that much mana of {@code color} in a single step (the counter removal is the ability's
 * cost, but modelling it together with the mana is observationally identical here — no timing or
 * trigger cares about the split for these cards). Implements {@link ManaProducingEffect} so the
 * engine treats the ability as a mana ability (CR 605.1a); it keeps the neutral estimator
 * defaults because the exact output is a player-chosen, special-routing amount.
 */
public record RemoveCountersForManaEffect(ManaColor color, CounterType counterType) implements ManaProducingEffect {
}
