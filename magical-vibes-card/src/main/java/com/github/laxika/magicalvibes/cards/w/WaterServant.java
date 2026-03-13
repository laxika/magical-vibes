package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "80")
public class WaterServant extends Card {

    public WaterServant() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new BoostSelfEffect(1, -1)), "{U}: Water Servant gets +1/-1 until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new BoostSelfEffect(-1, 1)), "{U}: Water Servant gets -1/+1 until end of turn."));
    }
}
