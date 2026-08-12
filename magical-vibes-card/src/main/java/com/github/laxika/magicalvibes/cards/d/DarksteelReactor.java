package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "114")
public class DarksteelReactor extends Card {

    public DarksteelReactor() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.CHARGE),
                        "Put a charge counter on Darksteel Reactor?"));

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentHasAtLeastCountersPredicate(CounterType.CHARGE, 20),
                List.of(new WinGameEffect()),
                "Darksteel Reactor's state-triggered ability"));
    }
}
