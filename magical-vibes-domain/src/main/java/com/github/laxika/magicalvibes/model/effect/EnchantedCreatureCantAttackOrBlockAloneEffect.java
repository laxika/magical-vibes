package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura effect: the enchanted creature can't attack alone, and optionally can't block
 * alone either.
 */
public record EnchantedCreatureCantAttackOrBlockAloneEffect(boolean restrictsBlocking)
        implements CardEffect {

    public EnchantedCreatureCantAttackOrBlockAloneEffect() {
        this(true);
    }
}
