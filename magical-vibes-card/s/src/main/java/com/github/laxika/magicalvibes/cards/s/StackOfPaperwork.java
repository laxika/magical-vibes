package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CombatDamageUsesStackEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "13")
public class StackOfPaperwork extends Card {

    public StackOfPaperwork() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addStaticEffect(new CombatDamageUsesStackEffect());
    }
}
