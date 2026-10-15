package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "79")
public class CarrionHowler extends Card {

    public CarrionHowler() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new BoostSelfEffect(2, -1)),
                "Pay 1 life: This creature gets +2/-1 until end of turn."));
    }
}
