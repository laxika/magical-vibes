package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SkimmingStrike;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.OmenCast;

@CardRegistration(set = "TDM", collectorNumber = "40")
public class DirgurIslandDragon extends Card {

    public DirgurIslandDragon() {
        setBackFaceCard(new SkimmingStrike());
        addCastingOption(new OmenCast());
    }

    @Override
    public String getBackFaceClassName() {
        return "SkimmingStrike";
    }
}
