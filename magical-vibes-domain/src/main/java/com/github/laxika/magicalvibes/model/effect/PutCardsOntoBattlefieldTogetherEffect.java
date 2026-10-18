package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Resolves {@code steps} in order, holding every card their {@link PutCardToBattlefieldEffect}s choose
 * from hand until all steps have chosen, then puts the held cards onto the battlefield as one event so
 * each entrant's abilities see the others enter (Michelangelo, Improviser's "a creature card and/or a
 * land card"). Held cards stay in hand and are not offered to later steps. Only the enter-tapped option
 * of a held put applies.
 */
public record PutCardsOntoBattlefieldTogetherEffect(List<CardEffect> steps) implements CardEffect {

    public PutCardsOntoBattlefieldTogetherEffect {
        steps = List.copyOf(steps);
    }
}
