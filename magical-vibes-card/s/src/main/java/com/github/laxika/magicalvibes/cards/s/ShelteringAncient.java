package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "121")
public class ShelteringAncient extends Card {

    public ShelteringAncient() {
        // Cumulative upkeep—Put a +1/+1 counter on a creature an opponent controls.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                CumulativeUpkeepEffect.putCounterOnOpponentCreature(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
