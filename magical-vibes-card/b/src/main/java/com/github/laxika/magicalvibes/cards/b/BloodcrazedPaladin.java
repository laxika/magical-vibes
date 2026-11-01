package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "XLN", collectorNumber = "93")
public class BloodcrazedPaladin extends Card {

    public BloodcrazedPaladin() {
        // Flash is loaded from Scryfall as a keyword.
        // This creature enters with a +1/+1 counter on it for each creature that died this turn.
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE,
                new CreatureDeathsThisTurn(CountScope.ANY_PLAYER)));
    }
}
