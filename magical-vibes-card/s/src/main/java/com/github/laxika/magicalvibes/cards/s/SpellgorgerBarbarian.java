package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "100")
public class SpellgorgerBarbarian extends Card {

    public SpellgorgerBarbarian() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER, true));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
