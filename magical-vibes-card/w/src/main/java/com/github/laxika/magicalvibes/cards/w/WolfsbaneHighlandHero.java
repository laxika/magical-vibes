package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "767")
public class WolfsbaneHighlandHero extends Card {

    public WolfsbaneHighlandHero() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new BoostSelfEffect(2, 2)),
                "{2}{G}: Wolfsbane gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
