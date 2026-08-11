package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "77")
public class AsphodelWanderer extends Card {

    public AsphodelWanderer() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{B}", List.of(new RegenerateEffect()),
                "{2}{B}: Regenerate Asphodel Wanderer."));
    }
}
