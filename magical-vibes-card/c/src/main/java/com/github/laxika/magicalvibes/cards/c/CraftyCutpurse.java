package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CraftyCutpurseEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "33")
@CardRegistration(set = "C21", collectorNumber = "117")
public class CraftyCutpurse extends Card {

    public CraftyCutpurse() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CraftyCutpurseEffect());
    }
}
