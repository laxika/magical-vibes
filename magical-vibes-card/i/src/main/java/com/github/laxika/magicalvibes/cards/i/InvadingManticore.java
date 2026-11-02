package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "134")
public class InvadingManticore extends Card {

    public InvadingManticore() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AmassGoblinsEffect(2, CardSubtype.ZOMBIE));
    }
}
