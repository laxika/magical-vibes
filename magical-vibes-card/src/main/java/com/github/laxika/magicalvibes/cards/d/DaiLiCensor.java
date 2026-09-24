package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "102")
public class DaiLiCensor extends Card {

    public DaiLiCensor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new BoostSelfEffect(2, 2)
                ),
                "Sacrifice another creature: This creature gets +2/+2 until end of turn. Activate only once each turn.",
                1
        ));
    }
}
