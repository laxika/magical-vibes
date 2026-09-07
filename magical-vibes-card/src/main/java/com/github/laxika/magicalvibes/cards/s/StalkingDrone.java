package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "124")
public class StalkingDrone extends Card {

    public StalkingDrone() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{C}",
                List.of(new BoostSelfEffect(1, 2)),
                "{C}: This creature gets +1/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
