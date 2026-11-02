package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "59")
@CardRegistration(set = "DDQ", collectorNumber = "25")
public class GryffVanguard extends Card {

    public GryffVanguard() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
    }
}
