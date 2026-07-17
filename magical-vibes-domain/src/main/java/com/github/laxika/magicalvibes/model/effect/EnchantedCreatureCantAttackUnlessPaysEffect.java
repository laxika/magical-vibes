package com.github.laxika.magicalvibes.model.effect;

/**
 * Static aura effect: the enchanted creature can't attack unless its controller pays {amount}
 * (generic mana) as an additional cost to declare it as an attacker. Brainwash ({3}).
 */
public record EnchantedCreatureCantAttackUnlessPaysEffect(int amount) implements CardEffect {
}
