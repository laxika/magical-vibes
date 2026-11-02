package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GoblinGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "219")
@CardRegistration(set = "HOP", collectorNumber = "59")
public class MenacingOgre extends Card {

    public MenacingOgre() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GoblinGameEffect(true, 2));
    }
}
