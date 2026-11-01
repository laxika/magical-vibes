package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "79")
public class AshioksReaper extends Card {

    public AshioksReaper() {
        addTrigger(Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                new DrawCardEffect());
    }
}
