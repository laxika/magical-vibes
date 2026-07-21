package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "INR", collectorNumber = "32")
public class LunarchVeteran extends Card {

    public LunarchVeteran() {
        LuminousPhantom backFace = new LuminousPhantom();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Whenever another creature you control enters, you gain 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));

        // Disturb {1}{W}
        addCastingOption(new DisturbCast("{1}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "LuminousPhantom";
    }
}
