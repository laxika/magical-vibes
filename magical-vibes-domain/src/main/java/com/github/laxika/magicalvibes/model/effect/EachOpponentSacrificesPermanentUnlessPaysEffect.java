package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent sacrifices a permanent of their choice unless they pay the specified mana cost.
 * Each opponent makes the choice independently in APNAP order.
 */
public record EachOpponentSacrificesPermanentUnlessPaysEffect(String manaCost) implements CardEffect {
}
