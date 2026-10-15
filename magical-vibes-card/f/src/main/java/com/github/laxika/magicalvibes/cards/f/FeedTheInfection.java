package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentWithPoisonCountersLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "ONE", collectorNumber = "93")
public class FeedTheInfection extends Card {

    public FeedTheInfection() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(3));
        addEffect(EffectSlot.SPELL, new EachOpponentWithPoisonCountersLosesLifeEffect(3, 3));
    }
}
