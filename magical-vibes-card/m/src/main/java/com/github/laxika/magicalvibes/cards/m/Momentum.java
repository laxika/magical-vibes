package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "113")
public class Momentum extends Card {

    public Momentum() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.GROWTH),
                        "Put a growth counter on Momentum?"))
                .addStaticEffect(new AttachedBoostEffect(
                        new CountersOnSource(CounterType.GROWTH),
                        new CountersOnSource(CounterType.GROWTH),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
