package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "148")
@CardRegistration(set = "WOE", collectorNumber = "352")
public class RotisserieElemental extends Card {

    public RotisserieElemental() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.SKEWER),
                new MayEffect(
                        new SacrificeSelfThenEffect(new ExileTopCardMayPlayThisTurnEffect(
                                new CountersOnSource(CounterType.SKEWER), false)),
                        "You may sacrifice Rotisserie Elemental.")));
    }
}
