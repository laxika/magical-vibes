package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "116")
public class BloodbraidMarauder extends Card {

    public BloodbraidMarauder() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withEventCondition(new Delirium()), new CascadeEffect());
    }
}
