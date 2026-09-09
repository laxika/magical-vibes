package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "VOW", collectorNumber = "184")
public class WearyPrisoner extends Card {

    public WearyPrisoner() {
        setBackFaceCard(new WrathfulJailbreaker());
    }

    @Override
    public String getBackFaceClassName() {
        return "WrathfulJailbreaker";
    }
}
