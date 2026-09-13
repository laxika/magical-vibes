package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AnimusOfNightsReach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;

@CardRegistration(set = "NEO", collectorNumber = "109")
public class TheLongReachOfNight extends Card {

    public TheLongReachOfNight() {
        setBackFaceCard(new AnimusOfNightsReach());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new EachOpponentSacrificesCreatureUnlessDiscardEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_II, new EachOpponentSacrificesCreatureUnlessDiscardEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "AnimusOfNightsReach";
    }
}
