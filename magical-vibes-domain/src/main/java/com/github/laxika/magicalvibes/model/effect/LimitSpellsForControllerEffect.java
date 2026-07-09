package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller of this permanent can't cast more than {@code maxSpells}
 * spells each turn (e.g. Colfenor's Plans: "You can't cast more than one spell each turn").
 *
 * <p>Unlike {@link LimitSpellsPerTurnEffect} (Rule of Law, which limits every player), this
 * only restricts the source's controller.
 */
public record LimitSpellsForControllerEffect(int maxSpells) implements CardEffect {
}
