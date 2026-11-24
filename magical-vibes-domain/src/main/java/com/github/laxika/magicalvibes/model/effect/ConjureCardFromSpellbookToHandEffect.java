package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * "Conjure a card of your choice from [this]'s spellbook into your hand", "conjure two cards of your choice from the
 * Three Pigs spellbook into your hand": the controller chooses {@code count} different cards among the listed
 * printings, which are conjured into their hand. The step pauses at its end on the choice and records
 * {@code StepKind.CONJURE} as it is answered, so "then put one of them onto the battlefield" is a later step over
 * {@code Subjects.cardsThisWay(StepKind.CONJURE)}. The log counts the cards and names none (CR 400.2).
 *
 * @param spellbook the printings offered
 * @param count     how many different cards are chosen
 */
public record ConjureCardFromSpellbookToHandEffect(List<CardPrintingReference> spellbook, int count)
        implements CardEffect {

    public ConjureCardFromSpellbookToHandEffect {
        spellbook = List.copyOf(spellbook);
        if (count < 1 || count > spellbook.size()) {
            throw new IllegalArgumentException("Spellbook choice count must be within the spellbook size");
        }
    }

    public ConjureCardFromSpellbookToHandEffect(List<CardPrintingReference> spellbook) {
        this(spellbook, 1);
    }

    public record CardPrintingReference(String setCode, String collectorNumber) {
    }
}
