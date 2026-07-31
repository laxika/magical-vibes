package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "87")
public class BloodBairn extends Card {

    public BloodBairn() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(false, false, false, true), new BoostSelfEffect(2, 2)),
                "Sacrifice another creature: Blood Bairn gets +2/+2 until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
