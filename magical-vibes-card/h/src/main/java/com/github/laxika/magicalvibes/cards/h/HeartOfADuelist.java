package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawFromAnywhereInLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "294")
@CardRegistration(set = "MB2", collectorNumber = "530")
public class HeartOfADuelist extends Card {

    public HeartOfADuelist() {
        addStaticEffect(new DrawFromAnywhereInLibraryEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
    }
}
