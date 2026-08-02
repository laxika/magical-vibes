package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "28")
public class ZarichiTiger extends Card {

    public ZarichiTiger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new GainLifeEffect(2)),
                "{1}{W}, {T}: You gain 2 life."
        ));
    }
}
