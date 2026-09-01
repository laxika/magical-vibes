package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "82")
public class WitnessOfTomorrows extends Card {

    public WitnessOfTomorrows() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{U}", List.of(new ScryEffect(1)),
                "{3}{U}: Scry 1."));
    }
}
