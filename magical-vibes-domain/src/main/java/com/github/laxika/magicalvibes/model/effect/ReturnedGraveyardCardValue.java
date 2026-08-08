package com.github.laxika.magicalvibes.model.effect;

/**
 * Which characteristic of the card returned by a preceding {@link ReturnCardFromGraveyardEffect}
 * a {@link RecordReturnedGraveyardCardValueEffect} records as the entry's event value.
 */
public enum ReturnedGraveyardCardValue {

    /**
     * The returned card's mana value, and only when it is a <b>nonland</b> card ("If you return a
     * nonland card to your hand this way" — Vengeful Rebirth); a returned land records 0.
     */
    NONLAND_MANA_VALUE,

    /**
     * The returned card's printed power ("equal to the power of the card returned this way" —
     * Morgue Burst); a card with no power (never a legal target for such a card) records 0.
     */
    POWER
}
