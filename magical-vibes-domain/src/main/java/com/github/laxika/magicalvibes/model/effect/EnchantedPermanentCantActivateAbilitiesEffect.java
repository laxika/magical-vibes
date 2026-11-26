package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura marker that prevents activated abilities of the enchanted permanent from being
 * activated (CR 602.5), optionally excluding mana abilities. "Enchanted creature's activated abilities can't be
 * activated" (Arrest, Ice Cage) is {@link #includingManaAbilities()}; "… unless they're mana abilities" is the
 * no-argument constructor.
 */
public record EnchantedPermanentCantActivateAbilitiesEffect(boolean blocksManaAbilities)
        implements EnchantedPermanentAbilityLockEffect {

    /** Prevents non-mana activated abilities while leaving mana abilities available. */
    public EnchantedPermanentCantActivateAbilitiesEffect() {
        this(false);
    }

    /** Prevents every activated ability of the enchanted permanent, mana abilities included. */
    public static EnchantedPermanentCantActivateAbilitiesEffect includingManaAbilities() {
        return new EnchantedPermanentCantActivateAbilitiesEffect(true);
    }
}
