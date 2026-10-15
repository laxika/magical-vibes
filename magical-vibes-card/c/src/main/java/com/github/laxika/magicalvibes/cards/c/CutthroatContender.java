package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "73")
public class CutthroatContender extends Card {

    public CutthroatContender() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new BoostSelfEffect(1, 0)),
                "Pay 1 life: This creature gets +1/+0 until end of turn. Activate only once each turn.",
                1));
    }
}
