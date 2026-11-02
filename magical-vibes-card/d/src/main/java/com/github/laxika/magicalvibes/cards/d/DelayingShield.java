package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DamageToControllerCounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DelayingShieldUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "17")
public class DelayingShield extends Card {

    public DelayingShield() {
        addStaticEffect(new DamageToControllerCounterReplacementEffect(CounterType.DELAY));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DelayingShieldUpkeepEffect());
    }
}
