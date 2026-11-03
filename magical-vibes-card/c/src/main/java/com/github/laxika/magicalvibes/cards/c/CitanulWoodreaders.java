package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "125")
@CardRegistration(set = "MMA", collectorNumber = "140")
@CardRegistration(set = "DDR", collectorNumber = "4")
@CardRegistration(set = "TSR", collectorNumber = "199")
public class CitanulWoodreaders extends Card {

    public CitanulWoodreaders() {
        addStaticEffect(new KickerEffect("{2}{G}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                new DrawCardEffect(2));
    }
}
