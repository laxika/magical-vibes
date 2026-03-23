package com.github.laxika.magicalvibes.model.effect;

/**
 * "You gain life equal to the greatest power among creatures you control."
 *
 * <p>At resolution, iterates the controller's battlefield to find the creature with the
 * highest effective power, then gains that much life. If the controller controls no
 * creatures, no life is gained.
 *
 * <p>Used by Huatli, Warrior Poet's +2 loyalty ability.
 */
public record GainLifeEqualToGreatestPowerAmongOwnCreaturesEffect() implements CardEffect {
}
