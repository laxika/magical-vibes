package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CasualtyCostPaid;
import com.github.laxika.magicalvibes.model.effect.CasualtyCost;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "49")
public class MakeDisappear extends Card {

    public MakeDisappear() {
        addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new CasualtyCostPaid()),
                new CopyTriggeringSpellEffect());
        addCastingCost(new CasualtyCost(1));
        addSpellEffect(new CounterUnlessPaysEffect(2));
    }
}
