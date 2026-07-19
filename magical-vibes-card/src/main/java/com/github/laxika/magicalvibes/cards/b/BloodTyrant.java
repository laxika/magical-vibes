package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifeAndPutCountersOnSourceEqualToLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "CON", collectorNumber = "99")
public class BloodTyrant extends Card {

    public BloodTyrant() {
        // At the beginning of your upkeep, each player loses 1 life. Put a +1/+1 counter on this
        // creature for each 1 life lost this way.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new EachPlayerLosesLifeAndPutCountersOnSourceEqualToLifeLostEffect(1));

        // Whenever a player loses the game, put five +1/+1 counters on this creature. (2-player engine:
        // the game ends before this trigger resolves — see WithengarUnbound.)
        addEffect(EffectSlot.ON_PLAYER_LOSES_GAME,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 5));
    }
}
