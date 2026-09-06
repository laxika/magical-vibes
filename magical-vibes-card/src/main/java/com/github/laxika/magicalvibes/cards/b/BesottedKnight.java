package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "WOE", collectorNumber = "4")
public class BesottedKnight extends Card {

    public BesottedKnight() {
        setBackFaceCard(new BetrothTheBeast());
        addCastingOption(new AdventureCast("{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BetrothTheBeast";
    }
}
