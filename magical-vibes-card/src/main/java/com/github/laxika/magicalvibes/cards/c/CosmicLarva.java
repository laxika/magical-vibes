package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "63")
public class CosmicLarva extends Card {

    public CosmicLarva() {
        // At the beginning of your upkeep, sacrifice this creature unless you sacrifice two lands.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(2, new PermanentIsLandPredicate()),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
