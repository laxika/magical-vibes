package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OnAlert;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "31")
public class SilverflameSquire extends Card {

    public SilverflameSquire() {
        setBackFaceCard(new OnAlert());
        addCastingOption(new AdventureCast("{2}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "OnAlert";
    }
}
