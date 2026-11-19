package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the controller's top library card and counters the spell that caused this triggered ability if the
 * two cards match in the way {@link #match()} names. The revealed card stays on top of the library. The ability
 * does not target the spell, so it still resolves after the spell left the stack; then only the counter is
 * skipped, and the spell is read from the stack entry's triggering spell (CR 608.2h).
 *
 * @param match                       how the revealed card is compared with the triggering spell
 * @param spellControllerMayCastFree  {@code true} when, on a match, the triggering spell's controller may cast
 *                                    the revealed nonland card without paying its mana cost (Swindler's Scheme)
 */
public record RevealTopCardAndCounterTriggeringSpellIfMatchesEffect(RevealedCardMatch match,
                                                                    boolean spellControllerMayCastFree)
        implements CounterSpellingEffect {

    public RevealTopCardAndCounterTriggeringSpellIfMatchesEffect {
        if (match == null) {
            throw new IllegalArgumentException("match cannot be null");
        }
    }
}
