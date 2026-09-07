package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.ProfaneInsight;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "90")
public class FoulmireKnight extends Card {

    public FoulmireKnight() {
        setBackFaceCard(new ProfaneInsight());
        addCastingOption(new AdventureCast("{2}{B}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "ProfaneInsight";
    }
}
