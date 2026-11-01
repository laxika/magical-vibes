package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "93")
public class HeraldOfTheDreadhorde extends Card {

    public HeraldOfTheDreadhorde() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new AmassGoblinsEffect(2, CardSubtype.ZOMBIE));
    }
}
