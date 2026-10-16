package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnControlledCreaturesSpacecraftsAndPlanetsEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "196")
public class LoadingZone extends Card {

    public LoadingZone() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{G}"))));
        addEffect(EffectSlot.STATIC,
                new DoubleCountersOnControlledCreaturesSpacecraftsAndPlanetsEffect());
    }
}
