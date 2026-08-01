package com.github.laxika.magicalvibes.model.effect;

/**
 * {@code ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD} marker (Death Watch): when the enchanted
 * creature dies, its controller loses life equal to its power and the Aura's controller gains life
 * equal to its toughness. {@code DeathTriggerCollectorService} snapshots both stats from last-known
 * information and bakes an {@link EnchantedCreatureControllerLosesLifeEffect} + {@link GainLifeEffect}
 * onto one stack entry (loss then gain), so SBAs see the net after both halves.
 */
public record EnchantedCreatureDiesLoseLifeEqualPowerGainLifeEqualToughnessEffect() implements CardEffect {
}
