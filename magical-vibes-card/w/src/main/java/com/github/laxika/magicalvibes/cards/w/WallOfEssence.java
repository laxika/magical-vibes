package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "41")
@CardRegistration(set = "STH", collectorNumber = "22")
@CardRegistration(set = "TPR", collectorNumber = "38")
public class WallOfEssence extends Card {

    public WallOfEssence() {
        addTrigger(Triggers.dealtCombatDamage(), new GainLifeEffect(new EventValue()));
    }
}
