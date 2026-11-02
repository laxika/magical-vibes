package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMC", collectorNumber = "29")
public class HighScore extends Card {

    public HighScore() {
        addStaticEffect(new AddOnePlusOneCountersEffect());
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ConditionalEffect(new ControlsCreatureWithGreatestPower(), new DrawCardEffect(), false));
    }
}
