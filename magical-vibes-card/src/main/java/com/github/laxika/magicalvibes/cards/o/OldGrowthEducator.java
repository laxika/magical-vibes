package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "SOS", collectorNumber = "207")
public class OldGrowthEducator extends Card {

    public OldGrowthEducator() {
        // Vigilance, reach are auto-loaded from Scryfall.
        // Infusion — When this creature enters, put two +1/+1 counters on it if you gained life this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new PutCountersOnSourceEffect(1, 1, 2)));
    }
}
