package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Put those cards onto the battlefield under their owners' control": each card {@code cards} names, in whichever zone
 * it names it in, enters under its owner's control (tapped when {@code tapped}), all together, an Aura attached as
 * that player chooses (CR 303.4f). A card that can't enter, or an Aura with nothing to enchant, stays where it is
 * (CR 303.4g). "Puts all land cards revealed this way onto the battlefield tapped" is
 * {@code cardsThisWay(StepKind.REVEAL, land)} (Clear the Land).
 *
 * @param cards  the cards put onto the battlefield
 * @param tapped whether they enter tapped
 */
public record PutCardsOntoBattlefieldEffect(CardSubject cards, boolean tapped) implements CardEffect {

    public PutCardsOntoBattlefieldEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved or revealed: " + cards);
        }
    }

    public PutCardsOntoBattlefieldEffect(CardSubject cards) {
        this(cards, false);
    }
}
