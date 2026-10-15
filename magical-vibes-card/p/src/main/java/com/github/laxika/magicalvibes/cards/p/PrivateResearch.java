package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "41")
public class PrivateResearch extends Card {

    public PrivateResearch() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.PAGE),
                        "Put a page counter on this Aura?"))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new DrawCardEffect(new CountersOnSource(CounterType.PAGE)));
    }
}
