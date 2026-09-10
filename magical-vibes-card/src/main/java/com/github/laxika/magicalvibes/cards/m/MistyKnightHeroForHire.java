package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "145")
public class MistyKnightHeroForHire extends Card {

    public MistyKnightHeroForHire() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DrawCardEffect(new CardsDiscardedOrCycledThisTurn())
                ),
                "{2}, {T}, Discard a card: Draw a card for each card you've discarded this turn."
        ));
    }
}
