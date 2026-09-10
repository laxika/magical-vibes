package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GoneFishing;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HOB", collectorNumber = "44")
public class LakeTownMariners extends Card {

    public LakeTownMariners() {
        setBackFaceCard(new GoneFishing());
        addCastingOption(new AdventureCast("{3}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GoneFishing";
    }
}
