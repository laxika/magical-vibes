package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M11", collectorNumber = "85")
@CardRegistration(set = "M13", collectorNumber = "83")
@CardRegistration(set = "ROE", collectorNumber = "98")
public class BloodthroneVampire extends Card {

    public BloodthroneVampire() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new BoostSelfEffect(2, 2)),
                "Sacrifice a creature: Bloodthrone Vampire gets +2/+2 until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
