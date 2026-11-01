package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "118")
public class WickedVisitor extends Card {

    public WickedVisitor() {
        addTrigger(Triggers.dies(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
