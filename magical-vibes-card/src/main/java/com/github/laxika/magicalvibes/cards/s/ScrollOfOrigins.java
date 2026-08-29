package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "159")
public class ScrollOfOrigins extends Card {

    public ScrollOfOrigins() {
        // {2}, {T}: Draw a card if you have seven or more cards in hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ConditionalEffect(new CardsInHandAtLeast(7), new DrawCardEffect(1))),
                "{2}, {T}: Draw a card if you have seven or more cards in hand."
        ));
    }
}
