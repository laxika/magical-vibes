package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "175")
public class RailwayBrawler extends Card {

    public RailwayBrawler() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new PutCountersOnEnteringCreatureEffect(new TargetPower(), false));

        // Plot {3}{G}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{G}"))));
    }
}
