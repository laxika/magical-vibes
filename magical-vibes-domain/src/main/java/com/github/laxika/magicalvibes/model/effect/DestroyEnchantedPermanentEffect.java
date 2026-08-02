package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the permanent that the source Aura is attached to. Resolved by
 * {@code DestroyEnchantedPermanentEffectHandler}. Used on
 * {@code ON_ENCHANTED_PERMANENT_TAPPED} (Spreading Algae), {@code ON_ANY_CREATURE_DIES}
 * (Yoke of the Damned), {@code ON_ENCHANTED_CREATURE_DEALT_DAMAGE} (Mortal Wound), and
 * {@code ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY} (Spinal Graft).
 * When {@code cannotBeRegenerated} is true, regeneration shields are ignored (Spinal Graft).
 */
public record DestroyEnchantedPermanentEffect(boolean cannotBeRegenerated) implements CardEffect {

    public DestroyEnchantedPermanentEffect() {
        this(false);
    }
}
