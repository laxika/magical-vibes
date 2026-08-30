package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GiftOfTheFae;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "11")
public class FaerieGuidemother extends Card {

    public FaerieGuidemother() {
        setBackFaceCard(new GiftOfTheFae());
        addCastingOption(new AdventureCast("{1}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GiftOfTheFae";
    }
}
