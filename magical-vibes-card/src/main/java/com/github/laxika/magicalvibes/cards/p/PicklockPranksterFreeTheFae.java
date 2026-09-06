package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FreeTheFae;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "64")
public class PicklockPranksterFreeTheFae extends Card {

    public PicklockPranksterFreeTheFae() {
        setBackFaceCard(new FreeTheFae());
        addCastingOption(new AdventureCast("{1}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "FreeTheFae";
    }
}
