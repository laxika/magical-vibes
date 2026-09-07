package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "76")
public class SelhoffEntomber extends Card {

    public SelhoffEntomber() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.CREATURE), "creature"),
                        new DrawCardEffect(1)
                ),
                "{T}, Discard a creature card: Draw a card."
        ));
    }
}
