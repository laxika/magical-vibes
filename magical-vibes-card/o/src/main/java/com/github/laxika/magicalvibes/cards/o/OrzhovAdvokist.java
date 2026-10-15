package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCountersOnCreatureEffect;

@CardRegistration(set = "C21", collectorNumber = "99")
@CardRegistration(set = "NCC", collectorNumber = "207")
@CardRegistration(set = "MKC", collectorNumber = "77")
@CardRegistration(set = "MIC", collectorNumber = "91")
@CardRegistration(set = "C16", collectorNumber = "3")
@CardRegistration(set = "CM2", collectorNumber = "34")
public class OrzhovAdvokist extends Card {

    public OrzhovAdvokist() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new EachPlayerMayPutCountersOnCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
