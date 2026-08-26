package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SeasonalRitual;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "ELD", collectorNumber = "174")
public class RosethornAcolyte extends Card {

    public RosethornAcolyte() {
        setBackFaceCard(new SeasonalRitual());
        addCastingOption(new AdventureCast("{G}"));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }

    @Override
    public String getBackFaceClassName() {
        return "SeasonalRitual";
    }
}
