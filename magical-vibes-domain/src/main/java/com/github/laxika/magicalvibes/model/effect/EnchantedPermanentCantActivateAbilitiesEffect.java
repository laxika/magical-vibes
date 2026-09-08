package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura marker that prevents activated abilities of the enchanted permanent from being
 * activated, optionally excluding mana abilities.
 */
public record EnchantedPermanentCantActivateAbilitiesEffect(boolean blocksManaAbilities)
        implements EnchantedPermanentAbilityLockEffect {

    /** Prevents non-mana activated abilities while leaving mana abilities available. */
    public EnchantedPermanentCantActivateAbilitiesEffect() {
        this(false);
    }
}
