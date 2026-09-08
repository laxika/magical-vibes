package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;

@CardRegistration(set = "MSH", collectorNumber = "173")
public class HulklingBurgeoningBruiser extends Card {

    public HulklingBurgeoningBruiser() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new EvolveTriggerEffect());
    }
}
