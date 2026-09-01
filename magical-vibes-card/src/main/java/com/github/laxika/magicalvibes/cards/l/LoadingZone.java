package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnControlledCreaturesSpacecraftsAndPlanetsEffect;

@CardRegistration(set = "EOE", collectorNumber = "196")
public class LoadingZone extends Card {

    public LoadingZone() {
        addEffect(EffectSlot.STATIC,
                new DoubleCountersOnControlledCreaturesSpacecraftsAndPlanetsEffect());
    }
}
