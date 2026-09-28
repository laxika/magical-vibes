package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the controller's top library card and counters the spell that caused this triggered
 * ability if the two cards share a card type. If they do, the triggering spell's controller may
 * cast the revealed nonland card without paying its mana cost.
 */
public record RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect()
        implements CounterSpellingEffect {
}
