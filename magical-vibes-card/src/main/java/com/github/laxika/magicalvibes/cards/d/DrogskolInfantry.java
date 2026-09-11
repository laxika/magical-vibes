package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;

@CardRegistration(set = "VOW", collectorNumber = "10")
public class DrogskolInfantry extends Card {

    public DrogskolInfantry() {
        setBackFaceCard(new DrogskolArmaments());

        addCastingOption(new DisturbCast("{3}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "DrogskolArmaments";
    }
}
