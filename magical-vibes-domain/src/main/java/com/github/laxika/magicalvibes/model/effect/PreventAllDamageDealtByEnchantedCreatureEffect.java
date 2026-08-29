package com.github.laxika.magicalvibes.model.effect;

/**
 * Static aura effect that prevents damage dealt by the enchanted creature. Damage dealt
 * <em>to</em> the enchanted creature is unaffected.
 */
public record PreventAllDamageDealtByEnchantedCreatureEffect(boolean combatOnly) implements CardEffect {

    public PreventAllDamageDealtByEnchantedCreatureEffect() {
        this(false);
    }
}
