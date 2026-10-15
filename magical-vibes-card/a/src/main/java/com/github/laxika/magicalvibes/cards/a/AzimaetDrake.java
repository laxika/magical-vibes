package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "53")
public class AzimaetDrake extends Card {

    public AzimaetDrake() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new BoostSelfEffect(1, 0)),
                "{U}: This creature gets +1/+0 until end of turn. Activate only once each turn.", 1));
    }
}
