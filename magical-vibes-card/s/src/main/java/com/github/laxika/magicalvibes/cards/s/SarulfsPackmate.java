package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "192")
public class SarulfsPackmate extends Card {

    public SarulfsPackmate() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addCastingOption(new ForetellCast("{1}{G}"));
    }
}
