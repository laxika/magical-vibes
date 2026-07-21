package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement: if this card would be put into a graveyard from anywhere, exile it instead.
 * Used on disturb back faces (e.g. Luminous Phantom). Checked in {@code GraveyardService}
 * and when a transformed permanent with this effect on its current face would die.
 */
public record ExileInsteadOfGraveyardReplacementEffect() implements CardEffect {
}
