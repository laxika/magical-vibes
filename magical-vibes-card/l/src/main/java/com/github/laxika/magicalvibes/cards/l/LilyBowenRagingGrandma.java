package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOrKeepOneAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "79")
@CardRegistration(set = "PIP", collectorNumber = "399")
@CardRegistration(set = "PIP", collectorNumber = "607")
@CardRegistration(set = "PIP", collectorNumber = "927")
public class LilyBowenRagingGrandma extends Card {

    public LilyBowenRagingGrandma() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DoublePlusOneCountersOrKeepOneAndGainLifeEffect(16));
    }
}
