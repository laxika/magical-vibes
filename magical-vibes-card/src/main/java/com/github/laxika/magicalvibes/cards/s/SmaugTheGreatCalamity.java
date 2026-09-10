package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HOB", collectorNumber = "109")
public class SmaugTheGreatCalamity extends Card {

    public SmaugTheGreatCalamity() {
        setBackFaceCard(new SpewFlame());
        addCastingOption(new AdventureCast("{4}{R}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SpewFlame";
    }
}
