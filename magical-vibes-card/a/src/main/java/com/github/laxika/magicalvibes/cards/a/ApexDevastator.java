package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M3C", collectorNumber = "220")
public class ApexDevastator extends Card {

    public ApexDevastator() {
        addTrigger(Triggers.youCastThisSpell(), TriggerMode.INDEPENDENT, new CascadeEffect());
        addTrigger(Triggers.youCastThisSpell(), TriggerMode.INDEPENDENT, new CascadeEffect());
        addTrigger(Triggers.youCastThisSpell(), TriggerMode.INDEPENDENT, new CascadeEffect());
        addTrigger(Triggers.youCastThisSpell(), TriggerMode.INDEPENDENT, new CascadeEffect());
    }
}
