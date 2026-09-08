package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the controller's top library card and counters the spell that caused this triggered
 * ability if the two cards have the same mana value. The revealed card remains on top of the
 * library.
 */
public record RevealTopCardAndCounterTriggeringSpellIfManaValueMatchesEffect()
        implements CounterSpellingEffect {
}
