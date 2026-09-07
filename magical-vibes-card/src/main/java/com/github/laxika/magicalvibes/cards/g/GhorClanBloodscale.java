package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "66")
public class GhorClanBloodscale extends Card {

    public GhorClanBloodscale() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new BoostSelfEffect(2, 2)),
                "{3}{G}: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
