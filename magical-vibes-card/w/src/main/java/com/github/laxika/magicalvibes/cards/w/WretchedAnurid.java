package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "183")
@CardRegistration(set = "DMR", collectorNumber = "109")
public class WretchedAnurid extends Card {

    public WretchedAnurid() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER), new LoseLifeEffect(1));
    }
}
