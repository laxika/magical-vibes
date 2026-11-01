package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "94")
@CardRegistration(set = "TPR", collectorNumber = "72")
public class ThalakosSeer extends Card {

    public ThalakosSeer() {
        // "When this creature leaves the battlefield, draw a card."
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
