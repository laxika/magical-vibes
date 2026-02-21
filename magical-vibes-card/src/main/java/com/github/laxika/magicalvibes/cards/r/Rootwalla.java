package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "293")
public class Rootwalla extends Card {

    public Rootwalla() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new BoostSelfEffect(2, 2)),
                false,
                "{1}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
