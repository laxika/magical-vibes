package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "STX", collectorNumber = "57")
public class TeachingsOfTheArchaics extends Card {

    public TeachingsOfTheArchaics() {
        // The number of cards is chosen once, from hand sizes before any card is drawn.
        addSpellEffect(new ConditionalEffect(
                new AnOpponentHasMoreCardsInHandThanController(),
                new DrawCardEffect(new FixedIfCondition(
                        new AnOpponentHasMoreCardsInHandThanController(4), 3, 2))));
    }
}
