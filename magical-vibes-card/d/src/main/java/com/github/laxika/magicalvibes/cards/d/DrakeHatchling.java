package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "76")
public class DrakeHatchling extends Card {

    public DrakeHatchling() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new BoostSelfEffect(1, 0)),
                "{U}: This creature gets +1/+0 until end of turn. Activate only once each turn.", 1));
    }
}
