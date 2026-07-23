package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

@CardRegistration(set = "ICE", collectorNumber = "75")
public class IllusionaryForces extends Card {

    public IllusionaryForces() {
        // Cumulative upkeep {U}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{U}"));
    }
}
