package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DauntlessAvenger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "4")
@CardRegistration(set = "DBL", collectorNumber = "4")
public class BereavedSurvivor extends Card {

    public BereavedSurvivor() {
        setBackFaceCard(new DauntlessAvenger());

        // When another creature you control dies, transform this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DauntlessAvenger";
    }
}
