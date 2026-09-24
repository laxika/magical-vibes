package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Offers three distinct cards from a spellbook and puts the chosen card into a destination. */
public record DraftCardFromSpellbookEffect(List<String> cardNames,
                                           DraftCardRecipient recipient,
                                           boolean revealChosenCard,
                                           boolean exileChosenCard,
                                           boolean putChosenCardOntoBattlefield,
                                           List<CardEffect> chosenCardEffects) implements CardEffect {

    public DraftCardFromSpellbookEffect(List<String> cardNames) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, false, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames, boolean exileChosenCard) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, exileChosenCard, false, List.of());
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames, boolean exileChosenCard,
                                        boolean putChosenCardOntoBattlefield) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, exileChosenCard,
                putChosenCardOntoBattlefield, List.of());
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames,
                                        List<CardEffect> chosenCardEffects) {
        this(cardNames, DraftCardRecipient.CONTROLLER, false, false, false, chosenCardEffects);
    }

    public DraftCardFromSpellbookEffect(List<String> cardNames,
                                        DraftCardRecipient recipient,
                                        boolean revealChosenCard) {
        this(cardNames, recipient, revealChosenCard, false, false, List.of());
    }

    public DraftCardFromSpellbookEffect {
        if (exileChosenCard && putChosenCardOntoBattlefield) {
            throw new IllegalArgumentException("A drafted card cannot be exiled and put onto the battlefield");
        }
        cardNames = List.copyOf(cardNames);
        chosenCardEffects = List.copyOf(chosenCardEffects);
    }
}
