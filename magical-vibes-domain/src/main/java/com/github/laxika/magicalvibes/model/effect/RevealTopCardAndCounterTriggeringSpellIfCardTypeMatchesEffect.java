package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the controller's top library card and counters the spell that caused this triggered
 * ability if the two cards share a card type. If they do, the triggering spell's controller may
 * cast the revealed nonland card without paying its mana cost. Once that spell has left the stack, its types and
 * controller are read from the stack entry's triggering spell (CR 608.2h).
 */
public record RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect() implements CounterSpellingEffect {

    @Override
    public boolean targetsTriggeringSpell() {
        return false;
    }
}
