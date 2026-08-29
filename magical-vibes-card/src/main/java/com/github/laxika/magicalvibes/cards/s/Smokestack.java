package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "USG", collectorNumber = "309")
public class Smokestack extends Card {

    public Smokestack() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.SOOT),
                "Put a soot counter on Smokestack?"));

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                new CountersOnSource(CounterType.SOOT),
                new PermanentTruePredicate(),
                SacrificeRecipient.ACTIVE_PLAYER));
    }
}
