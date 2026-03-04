package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Modal spell effect: prompts the controller to choose one of the given options,
 * then resolves only the chosen option's effect.
 * <p>
 * Used by cards like Slagstorm: "Choose one — Slagstorm deals 3 damage to each creature.
 * — Slagstorm deals 3 damage to each player."
 */
public record ChooseOneEffect(List<ChooseOneOption> options) implements CardEffect {

    public record ChooseOneOption(String label, CardEffect effect) {}
}
