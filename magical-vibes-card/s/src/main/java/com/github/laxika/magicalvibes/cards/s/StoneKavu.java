package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "93")
public class StoneKavu extends Card {

    public StoneKavu() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(BoostEffect.self(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(BoostEffect.self(0, 1)),
                "{W}: This creature gets +0/+1 until end of turn."
        ));
    }
}
