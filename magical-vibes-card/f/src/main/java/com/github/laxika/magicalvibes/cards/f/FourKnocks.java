package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "20")
@CardRegistration(set = "WHO", collectorNumber = "340")
@CardRegistration(set = "WHO", collectorNumber = "625")
@CardRegistration(set = "WHO", collectorNumber = "931")
public class FourKnocks extends Card {

    public FourKnocks() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(4)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new DrawCardEffect(1));
    }
}
