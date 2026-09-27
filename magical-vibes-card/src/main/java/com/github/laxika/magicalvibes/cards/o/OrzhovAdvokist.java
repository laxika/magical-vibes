package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect;

@CardRegistration(set = "C21", collectorNumber = "99")
public class OrzhovAdvokist extends Card {

    public OrzhovAdvokist() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
