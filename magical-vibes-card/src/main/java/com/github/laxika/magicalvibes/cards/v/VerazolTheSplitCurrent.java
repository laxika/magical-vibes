package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "239")
public class VerazolTheSplitCurrent extends Card {

    public VerazolTheSplitCurrent() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new ManaSpentToCast()));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new KickedSpellCastTriggerEffect(List.of(
                        new RemoveCounterFromSourceThenEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 2, new CopyTriggeringSpellEffect(true)))),
                "Remove two +1/+1 counters from Verazol to copy that spell?"));
    }
}
