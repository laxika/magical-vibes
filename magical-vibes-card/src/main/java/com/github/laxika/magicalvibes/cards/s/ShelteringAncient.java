package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

@CardRegistration(set = "CSP", collectorNumber = "121")
public class ShelteringAncient extends Card {

    public ShelteringAncient() {
        // Cumulative upkeep—Put a +1/+1 counter on a creature an opponent controls.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                CumulativeUpkeepEffect.putCounterOnOpponentCreature(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
