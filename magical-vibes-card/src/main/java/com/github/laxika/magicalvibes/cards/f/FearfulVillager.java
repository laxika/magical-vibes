package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.FearsomeWerewolf;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "VOW", collectorNumber = "157")
public class FearfulVillager extends Card {

    public FearfulVillager() {
        setBackFaceCard(new FearsomeWerewolf());
    }

    @Override
    public String getBackFaceClassName() {
        return "FearsomeWerewolf";
    }
}
