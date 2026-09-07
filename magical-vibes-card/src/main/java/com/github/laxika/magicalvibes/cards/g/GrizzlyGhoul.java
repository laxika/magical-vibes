package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "INR", collectorNumber = "240")
@CardRegistration(set = "MID", collectorNumber = "226")
public class GrizzlyGhoul extends Card {

    public GrizzlyGhoul() {
        // Trample is loaded from Scryfall as a keyword.
        // This creature enters with a +1/+1 counter on it for each creature that died this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE,
                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
    }
}
