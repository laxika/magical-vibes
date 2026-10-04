package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker restricting Aura attachment, or only Aura spell targeting when
 * {@code auraSpellsOnly} is true. A targeting-only restriction permits Auras to enter attached
 * without being cast, and permits attachment effects that do not target with an Aura spell.
 *
 * @param auraSpellsOnly whether only targeting by Aura spells is prohibited
 */
public record CantBeEnchantedByOtherAurasEffect(boolean auraSpellsOnly) implements CardEffect {
    public CantBeEnchantedByOtherAurasEffect() {
        this(false);
    }
}
