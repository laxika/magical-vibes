package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MH1", collectorNumber = "41")
public class BazaarTrademage extends Card {

    public BazaarTrademage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(2),
                new DiscardEffect(3, Subjects.you()));
    }
}
