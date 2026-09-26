package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;

@CardRegistration(set = "RTR", collectorNumber = "217")
@CardRegistration(set = "GK1", collectorNumber = "113")
@CardRegistration(set = "PIO", collectorNumber = "308")
@CardRegistration(set = "SLD", collectorNumber = "2444")
public class GrowingRanks extends Card {

    public GrowingRanks() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PopulateEffect());
    }
}
