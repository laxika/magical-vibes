package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AnimusOfNightsReach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "109")
public class TheLongReachOfNight extends Card {

    public TheLongReachOfNight() {
        setBackFaceCard(new AnimusOfNightsReach());

        addTrigger(Triggers.chapter(1), new EachOpponentSacrificesCreatureUnlessDiscardEffect());
        addTrigger(Triggers.chapter(2), new EachOpponentSacrificesCreatureUnlessDiscardEffect());
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "AnimusOfNightsReach";
    }
}
