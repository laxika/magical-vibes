package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static Aura effect that restricts activated abilities of its enchanted
 * permanent.
 */
public interface EnchantedPermanentAbilityLockEffect extends CardEffect {

    /** Whether the restriction also applies to mana abilities. */
    boolean blocksManaAbilities();
}
