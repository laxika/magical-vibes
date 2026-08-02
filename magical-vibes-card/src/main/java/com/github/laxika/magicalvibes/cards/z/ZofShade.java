package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "125")
public class ZofShade extends Card {

    public ZofShade() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(new BoostSelfEffect(2, 2)), "{2}{B}: This creature gets +2/+2 until end of turn."));
    }
}
