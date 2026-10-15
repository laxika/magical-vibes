package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "PIP", collectorNumber = "109")
@CardRegistration(set = "PIP", collectorNumber = "418")
@CardRegistration(set = "PIP", collectorNumber = "637")
@CardRegistration(set = "PIP", collectorNumber = "946")
public class MarcusMutantMayor extends Card {

    public MarcusMutantMayor() {
        // A creature with a +1/+1 counter draws a card when it deals combat damage to a player.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)));

        // A creature without a +1/+1 counter gets one instead.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentNotPredicate(new PermanentHasCountersPredicate(
                                CounterType.PLUS_ONE_PLUS_ONE)),
                        new PutCountersOnSourceEffect(1, 1, 1), true));
    }
}
