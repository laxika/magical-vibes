package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TillAndTend;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HOB", collectorNumber = "118")
public class BeornReluctantHost extends Card {

    public BeornReluctantHost() {
        setBackFaceCard(new TillAndTend());
        addCastingOption(new AdventureCast("{1}{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "TillAndTend";
    }
}
