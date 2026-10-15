package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "71")
public class HavocSower extends Card {

    public HavocSower() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{C}",
                List.of(new BoostSelfEffect(2, 1)),
                "{1}{C}: This creature gets +2/+1 until end of turn."
        ));
    }
}
