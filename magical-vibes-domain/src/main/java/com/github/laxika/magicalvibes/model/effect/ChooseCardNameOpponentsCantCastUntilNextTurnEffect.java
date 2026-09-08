package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Spell effect: choose a card name; until your next turn, your opponents can't cast spells with
 * the chosen name (Comply, Academic Probation). Prompts for the name on resolution; the lock is
 * stamped on {@code GameData.opponentsCantCastNamedSpellsUntilControllerNextTurn} and cleared at
 * the start of the controller's next turn.
 */
public record ChooseCardNameOpponentsCantCastUntilNextTurnEffect(List<CardType> excludedTypes)
        implements CardEffect {

    public ChooseCardNameOpponentsCantCastUntilNextTurnEffect() {
        this(List.of());
    }

    public ChooseCardNameOpponentsCantCastUntilNextTurnEffect {
        excludedTypes = List.copyOf(excludedTypes);
    }
}
