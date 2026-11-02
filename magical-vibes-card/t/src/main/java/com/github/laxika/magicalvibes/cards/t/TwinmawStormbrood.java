package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CharringBite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "232")
public class TwinmawStormbrood extends Card {

    public TwinmawStormbrood() {
        setBackFaceCard(new CharringBite());
        addCastingOption(new OmenCast());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(5));
    }

    @Override
    public String getBackFaceClassName() {
        return "CharringBite";
    }
}
