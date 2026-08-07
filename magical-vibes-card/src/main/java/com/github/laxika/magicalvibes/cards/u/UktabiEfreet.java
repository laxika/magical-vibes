package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

@CardRegistration(set = "WTH", collectorNumber = "143")
public class UktabiEfreet extends Card {

    public UktabiEfreet() {
        // Cumulative upkeep {G}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{G}"));
    }
}
