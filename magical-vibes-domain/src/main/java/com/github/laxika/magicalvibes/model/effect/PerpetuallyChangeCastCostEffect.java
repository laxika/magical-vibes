package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "That card perpetually gains 'This spell costs {1} less to cast'", "it perpetually costs {1} more to cast": each
 * card {@code cards} names costs {@code change} generic mana more to cast, less when it is negative, from now on
 * (digital only). After a return to hand it is {@code Subjects.cardsThisWay(StepKind.RETURN)}.
 *
 * @param cards  the cards an earlier step moved
 * @param change how much more generic mana they cost; negative for less
 */
public record PerpetuallyChangeCastCostEffect(CardSubject cards, int change) implements CardEffect {

    public PerpetuallyChangeCastCostEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
        if (change == 0) {
            throw new IllegalArgumentException("A cost change changes the cost");
        }
    }
}
