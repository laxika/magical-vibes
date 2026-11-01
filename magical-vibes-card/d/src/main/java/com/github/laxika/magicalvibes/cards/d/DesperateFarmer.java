package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "105")
@CardRegistration(set = "VOW", collectorNumber = "104")
@CardRegistration(set = "DBL", collectorNumber = "371")
public class DesperateFarmer extends Card {

    public DesperateFarmer() {
        setBackFaceCard(new DepravedHarvester());

        // When another creature you control dies, transform this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "DepravedHarvester";
    }
}
