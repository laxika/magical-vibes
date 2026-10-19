package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/**
 * Reveals the controller's top library card and counters the spell that caused this triggered
 * ability if the two cards share a card type. If they do, the triggering spell's controller may
 * cast the revealed nonland card without paying its mana cost.
 *
 * @param spellSnapshot the triggering spell as it was when the ability triggered, used as last known
 *                      information when that spell has already left the stack; {@code null} until the
 *                      trigger is collected
 */
public record RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect(StackEntry spellSnapshot)
        implements CounterSpellingEffect {

    public RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect() {
        this(null);
    }
}
