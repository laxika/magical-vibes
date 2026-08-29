package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "USG", collectorNumber = "165")
public class Vebulid extends Card {

    public Vebulid() {
        // This creature enters with a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        // At the beginning of your upkeep, you may put a +1/+1 counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                "Put a +1/+1 counter on Vebulid?"));

        // When this creature attacks or blocks, destroy it at end of combat.
        addEffect(EffectSlot.ON_ATTACK, new DestroySelfAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new DestroySelfAtEndOfCombatEffect());
    }
}
