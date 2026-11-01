package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "32")
@CardRegistration(set = "INR", collectorNumber = "450")
@CardRegistration(set = "MID", collectorNumber = "27")
@CardRegistration(set = "DBL", collectorNumber = "27")
public class LunarchVeteran extends Card {

    public LunarchVeteran() {
        setBackFaceCard(new LuminousPhantom());

        // Whenever another creature you control enters, you gain 1 life.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), new GainLifeEffect(1));

        // Disturb {1}{W}
        addCastingOption(new DisturbCast("{1}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "LuminousPhantom";
    }
}
