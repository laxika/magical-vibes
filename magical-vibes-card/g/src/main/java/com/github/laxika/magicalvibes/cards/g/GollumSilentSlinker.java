package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MeagerMeal;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "HOB", collectorNumber = "71")
public class GollumSilentSlinker extends Card {

    public GollumSilentSlinker() {
        setBackFaceCard(new MeagerMeal());
        addCastingOption(new AdventureCast("{B}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "MeagerMeal";
    }
}
