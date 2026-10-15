package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CastOff;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "26")
@CardRegistration(set = "ELD", collectorNumber = "277")
@CardRegistration(set = "AFC", collectorNumber = "70")
@CardRegistration(set = "BLC", collectorNumber = "149")
@CardRegistration(set = "WOC", collectorNumber = "71")
public class RealmCloakedGiant extends Card {

    public RealmCloakedGiant() {
        setBackFaceCard(new CastOff());
        addCastingOption(new AdventureCast("{3}{W}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "CastOff";
    }
}
