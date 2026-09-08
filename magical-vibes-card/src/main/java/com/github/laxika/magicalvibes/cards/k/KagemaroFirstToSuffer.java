package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "73")
public class KagemaroFirstToSuffer extends Card {

    public KagemaroFirstToSuffer() {
        CardsInHand handSize = new CardsInHand(CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new BoostAllCreaturesEffect(new Scaled(handSize, -1), new Scaled(handSize, -1))
                ),
                "{B}, Sacrifice Kagemaro: All creatures get -X/-X until end of turn, where X is the number of cards in your hand."
        ));
    }
}
