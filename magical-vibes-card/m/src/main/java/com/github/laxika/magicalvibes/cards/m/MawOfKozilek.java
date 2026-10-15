package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "99")
public class MawOfKozilek extends Card {

    public MawOfKozilek() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{C}",
                List.of(new BoostSelfEffect(2, -2)),
                "{C}: This creature gets +2/-2 until end of turn."
        ));
    }
}
