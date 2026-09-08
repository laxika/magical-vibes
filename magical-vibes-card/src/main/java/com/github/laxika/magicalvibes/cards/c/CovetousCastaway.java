package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GhostlyCastigator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "INR", collectorNumber = "58")
@CardRegistration(set = "INR", collectorNumber = "456")
@CardRegistration(set = "MID", collectorNumber = "45")
public class CovetousCastaway extends Card {

    public CovetousCastaway() {
        setBackFaceCard(new GhostlyCastigator());

        // When this creature dies, mill three cards.
        addEffect(EffectSlot.ON_DEATH, new MillEffect(3, MillRecipient.CONTROLLER));

        // Disturb {3}{U}{U}
        addCastingOption(new DisturbCast("{3}{U}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GhostlyCastigator";
    }
}
