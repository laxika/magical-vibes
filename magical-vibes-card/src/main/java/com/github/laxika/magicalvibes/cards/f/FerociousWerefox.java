package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GuardChange;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "170")
public class FerociousWerefox extends Card {

    public FerociousWerefox() {
        setBackFaceCard(new GuardChange());
        addCastingOption(new AdventureCast("{1}{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GuardChange";
    }
}
