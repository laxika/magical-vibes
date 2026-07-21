package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;

@CardRegistration(set = "INR", collectorNumber = "72")
public class LanternBearer extends Card {

    public LanternBearer() {
        LanternsLift backFace = new LanternsLift();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Flying is auto-loaded from Scryfall keywords.

        // Disturb {2}{U}
        addCastingOption(new DisturbCast("{2}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "LanternsLift";
    }
}
