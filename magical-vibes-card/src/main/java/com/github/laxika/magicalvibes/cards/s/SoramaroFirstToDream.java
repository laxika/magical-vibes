package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "58")
public class SoramaroFirstToDream extends Card {

    public SoramaroFirstToDream() {
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(cardsInHand, cardsInHand));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        new DrawCardEffect(1)
                ),
                "{4}, Return a land you control to its owner's hand: Draw a card."
        ));
    }
}
