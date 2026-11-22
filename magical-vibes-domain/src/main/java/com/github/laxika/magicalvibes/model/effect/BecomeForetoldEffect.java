package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "It becomes foretold. Its foretell cost is its mana cost reduced by {N}" after an exile step: each card
 * {@code cards} names that is in exile becomes foretold by the effect's controller, who didn't foretell it.
 *
 * @param cards     which cards
 * @param reducedBy how much generic mana less than its mana cost the foretell cost is
 */
public record BecomeForetoldEffect(CardSubject cards, int reducedBy) implements CardEffect {

    public BecomeForetoldEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards.declaresTarget() || reducedBy < 0) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
    }
}
