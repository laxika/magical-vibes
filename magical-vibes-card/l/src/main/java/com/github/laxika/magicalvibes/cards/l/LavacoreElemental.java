package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "103")
public class LavacoreElemental extends Card {

    public LavacoreElemental() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(1)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME)),
                RemoveCountersEffect.fromSelf(CounterType.TIME, 1));

        // Whenever a creature you control deals combat damage to a player, put a time counter on this creature.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                PutCountersEffect.onSelf(CounterType.TIME));
    }
}
