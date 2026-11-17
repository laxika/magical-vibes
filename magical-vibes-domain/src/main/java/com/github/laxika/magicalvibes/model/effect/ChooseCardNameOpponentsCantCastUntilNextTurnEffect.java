package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Spell effect: choose a card name; until your next turn, your opponents can't cast spells with
 * the chosen name (Comply, Academic Probation). Prompts for the name on resolution; the lock is
 * made a floating effect of this record carrying the chosen {@code cardName}, which ends at the start of the
 * controller's next turn.
 */
public record ChooseCardNameOpponentsCantCastUntilNextTurnEffect(List<CardType> excludedTypes, String cardName)
        implements CardEffect {

    public ChooseCardNameOpponentsCantCastUntilNextTurnEffect(List<CardType> excludedTypes) {
        this(excludedTypes, null);
    }

    public ChooseCardNameOpponentsCantCastUntilNextTurnEffect() {
        this(List.of(), null);
    }

    public ChooseCardNameOpponentsCantCastUntilNextTurnEffect {
        excludedTypes = List.copyOf(excludedTypes);
    }
}
