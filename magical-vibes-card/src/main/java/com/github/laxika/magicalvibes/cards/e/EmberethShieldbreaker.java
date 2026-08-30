package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BattleDisplay;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ELD", collectorNumber = "122")
public class EmberethShieldbreaker extends Card {

    public EmberethShieldbreaker() {
        setBackFaceCard(new BattleDisplay());
        addCastingOption(new AdventureCast("{R}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BattleDisplay";
    }
}
