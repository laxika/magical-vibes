package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DesperateParry;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "63")
public class ObyrasAttendants extends Card {

    public ObyrasAttendants() {
        setBackFaceCard(new DesperateParry());
        addCastingOption(new AdventureCast("{1}{U}"));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "DesperateParry";
    }
}
