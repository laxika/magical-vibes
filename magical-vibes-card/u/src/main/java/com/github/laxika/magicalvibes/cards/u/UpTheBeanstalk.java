package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "195")
public class UpTheBeanstalk extends Card {

    public UpTheBeanstalk() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(5)), new DrawCardEffect());
    }
}
