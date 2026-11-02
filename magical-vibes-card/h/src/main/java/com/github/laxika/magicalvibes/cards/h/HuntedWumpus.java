package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "269")
@CardRegistration(set = "9ED", collectorNumber = "248")
@CardRegistration(set = "8ED", collectorNumber = "258")
@CardRegistration(set = "MMQ", collectorNumber = "253")
public class HuntedWumpus extends Card {

    public HuntedWumpus() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new OpponentMayPlayCreatureEffect());
    }
}
