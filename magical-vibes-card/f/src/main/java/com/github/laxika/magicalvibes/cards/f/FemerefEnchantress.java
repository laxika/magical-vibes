package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "129")
public class FemerefEnchantress extends Card {

    public FemerefEnchantress() {
        addTrigger(Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.ANY), new DrawCardEffect());
    }
}
