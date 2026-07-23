package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "119")
@CardRegistration(set = "ICE", collectorNumber = "95")
public class SeaSpirit extends Card {

    public SeaSpirit() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(new BoostSelfEffect(1, 0)), "{U}: This creature gets +1/+0 until end of turn."));
    }
}
