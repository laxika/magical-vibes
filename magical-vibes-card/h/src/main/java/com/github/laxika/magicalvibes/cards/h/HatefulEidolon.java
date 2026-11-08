package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AurasAttachedToTriggeringPermanent;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "101")
@CardRegistration(set = "SOC", collectorNumber = "215")
public class HatefulEidolon extends Card {

    public HatefulEidolon() {
        // Whenever an enchanted creature dies, draw a card for each Aura you controlled that was attached to it.
        addTrigger(Triggers.creatureDies(new PermanentIsEnchantedPredicate(), ObjectRelation.ANY),
                new DrawCardEffect(new AurasAttachedToTriggeringPermanent(true)));
    }
}
