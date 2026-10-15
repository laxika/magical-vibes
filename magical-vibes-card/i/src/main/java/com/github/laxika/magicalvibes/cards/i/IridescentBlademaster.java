package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "195")
public class IridescentBlademaster extends Card {

    public IridescentBlademaster() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{G}", List.of(new BoostSelfEffect(2, 2)),
                "{3}{G}: This creature gets +2/+2 until end of turn."));
    }
}
