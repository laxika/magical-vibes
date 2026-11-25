package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "165")
public class Vebulid extends Card {

    public Vebulid() {
        // This creature enters with a +1/+1 counter on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        // At the beginning of your upkeep, you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                "Put a +1/+1 counter on Vebulid?"));

        // When this creature attacks or blocks, destroy it at end of combat.
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE,
                new DestroyTargetPermanentAtEndOfCombatEffect(Subjects.source()));
    }
}
