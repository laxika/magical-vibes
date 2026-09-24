package com.github.laxika.magicalvibes.model.effect;

/**
 * Fixed additional mana cost for an entwined modal spell. The spell's modal definition supplies
 * the base and all-mode counts; the cost is paid only when all modes are selected.
 */
public record EntwineManaCost(String manaCost) implements CostEffect {
}
