package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "118")
public class SunCollaredRaptor extends Card {

    public SunCollaredRaptor() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}", List.of(new BoostSelfEffect(3, 0)),
                "{2}{R}: This creature gets +3/+0 until end of turn."));
    }
}
