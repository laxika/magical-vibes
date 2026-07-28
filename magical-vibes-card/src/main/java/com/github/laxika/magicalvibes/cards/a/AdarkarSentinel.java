package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "306")
public class AdarkarSentinel extends Card {

    public AdarkarSentinel() {
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new BoostSelfEffect(0, 1)), "{1}: Adarkar Sentinel gets +0/+1 until end of turn."));
    }
}
