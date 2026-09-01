package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each opponent who attacked with a creature this turn can't cast spells.
 * Used by Angelic Arbiter (M11), and with the scoped form by Sandswirl Wanderglyph.
 */
public record OpponentsCantCastSpellsIfAttackedThisTurnEffect(boolean onlyIfAttackedControllerOrPlaneswalker)
        implements CardEffect {

    public OpponentsCantCastSpellsIfAttackedThisTurnEffect() {
        this(false);
    }
}
