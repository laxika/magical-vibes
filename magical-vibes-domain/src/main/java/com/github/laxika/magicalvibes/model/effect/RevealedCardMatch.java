package com.github.laxika.magicalvibes.model.effect;

/**
 * What a {@link RevealTopCardAndCounterTriggeringSpellIfMatchesEffect} compares between the revealed card and
 * the triggering spell.
 */
public enum RevealedCardMatch {
    /** The revealed card's mana value equals the spell's, X included (Counterbalance). */
    SAME_MANA_VALUE,
    /** The revealed card shares a card type with the spell (Swindler's Scheme). */
    SHARES_CARD_TYPE
}
