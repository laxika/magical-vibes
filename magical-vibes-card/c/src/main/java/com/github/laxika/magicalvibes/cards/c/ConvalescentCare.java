package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "14")
public class ConvalescentCare extends Card {

    public ConvalescentCare() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerLifeAtMost(5)),
                SequenceEffect.of(new GainLifeEffect(3), new DrawCardEffect()));
    }
}
