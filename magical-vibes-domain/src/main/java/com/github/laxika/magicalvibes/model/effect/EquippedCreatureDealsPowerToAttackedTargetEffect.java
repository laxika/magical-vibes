package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever equipped creature attacks, it deals damage equal to its power to the player or
 * planeswalker it's attacking." (Mage Slayer)
 *
 * <p>Placed on the {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_ATTACK} slot of an
 * Equipment. Non-targeting: the recipient is the player/planeswalker the equipped creature is
 * attacking (read from the creature's combat state at resolution), never a chosen target. The
 * equipped creature is the source of the damage (so its power sets the amount and its keywords —
 * e.g. infect — apply), even though the ability itself belongs to the Equipment.</p>
 */
public record EquippedCreatureDealsPowerToAttackedTargetEffect() implements CardEffect {
}
