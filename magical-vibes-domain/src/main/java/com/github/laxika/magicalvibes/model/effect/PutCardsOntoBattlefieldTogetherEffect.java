package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Resolves {@code steps} in order, holding every card their {@link PutCardToBattlefieldEffect}s choose
 * from hand until all steps have chosen, then puts the held cards onto the battlefield as one event so
 * each entrant's abilities see the others enter (Michelangelo, Improviser's "a creature card and/or a
 * land card"). Held cards stay in hand and are not offered to later steps. Only the enter-tapped option
 * of a held put applies.
 *
 * <p>With {@code pickedCardsLeaveHand} a picked card is taken out of the hand at once, so later steps number the
 * remaining hand cards without it (Patron of the Moon); the cards return to the hand just before entering together.
 */
public record PutCardsOntoBattlefieldTogetherEffect(List<CardEffect> steps, boolean pickedCardsLeaveHand)
        implements CardEffect {

    public PutCardsOntoBattlefieldTogetherEffect {
        steps = List.copyOf(steps);
    }

    public PutCardsOntoBattlefieldTogetherEffect(List<CardEffect> steps) {
        this(steps, false);
    }
}
