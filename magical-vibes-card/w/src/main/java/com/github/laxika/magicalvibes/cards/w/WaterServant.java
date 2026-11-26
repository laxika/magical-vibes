package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "80")
@CardRegistration(set = "M14", collectorNumber = "80")
@CardRegistration(set = "MM2", collectorNumber = "69")
public class WaterServant extends Card {

    public WaterServant() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(BoostEffect.self(1, -1)), "{U}: Water Servant gets +1/-1 until end of turn."));
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(BoostEffect.self(-1, 1)), "{U}: Water Servant gets -1/+1 until end of turn."));
    }
}
