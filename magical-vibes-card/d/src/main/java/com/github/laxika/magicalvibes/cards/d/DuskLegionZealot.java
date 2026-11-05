package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "70")
@CardRegistration(set = "A25", collectorNumber = "89")
@CardRegistration(set = "GN3", collectorNumber = "49")
@CardRegistration(set = "LCC", collectorNumber = "194")
public class DuskLegionZealot extends Card {

    public DuskLegionZealot() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(), new LoseLifeEffect(1));
    }
}
