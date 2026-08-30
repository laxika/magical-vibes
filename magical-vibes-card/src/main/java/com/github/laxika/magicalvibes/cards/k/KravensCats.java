package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "104")
public class KravensCats extends Card {

    public KravensCats() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new BoostSelfEffect(2, 2)),
                "{2}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
