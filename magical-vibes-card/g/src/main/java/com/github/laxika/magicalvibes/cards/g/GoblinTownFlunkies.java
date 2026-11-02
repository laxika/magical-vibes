package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "100")
public class GoblinTownFlunkies extends Card {

    public GoblinTownFlunkies() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AmassGoblinsEffect(1));
    }
}
