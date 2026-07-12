package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "45")
public class ParapetWatchers extends Card {

    public ParapetWatchers() {
        addActivatedAbility(new ActivatedAbility(false, "{W/U}", List.of(new BoostSelfEffect(0, 1)), "{W/U}: Parapet Watchers gets +0/+1 until end of turn."));
    }
}
