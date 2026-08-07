package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement: if this card would be put into a graveyard, exile it instead.
 * Used on disturb back faces (e.g. Luminous Phantom). Checked in {@code GraveyardService}
 * and when a transformed permanent with this effect on its current face would die.
 *
 * @param dyingOnly when {@code true} the replacement applies only to a move from the
 *                  battlefield to a graveyard ("if this creature would die, exile it
 *                  instead" — Possessed Skaab); when {@code false} it applies from anywhere.
 */
public record ExileInsteadOfGraveyardReplacementEffect(boolean dyingOnly) implements CardEffect {

    public ExileInsteadOfGraveyardReplacementEffect() {
        this(false);
    }
}
