package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "76")
public class MerfolkSeer extends Card {

    public MerfolkSeer() {
        // When this creature dies, you may pay {1}{U}. If you do, draw a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayPayManaEffect("{1}{U}", new DrawCardEffect(1),
                "Pay {1}{U} to draw a card?"));
    }
}
