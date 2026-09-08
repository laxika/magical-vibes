package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "17")
public class KitsuneLoreweaver extends Card {

    public KitsuneLoreweaver() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new BoostSelfEffect(new Fixed(0), new CardsInHand(CountScope.CONTROLLER))),
                "{1}{W}: This creature gets +0/+X until end of turn, where X is the number of cards in your hand."
        ));
    }
}
