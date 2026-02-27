package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;

@CardRegistration(set = "SOM", collectorNumber = "73")
public class PainfulQuandary extends Card {

    public PainfulQuandary() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new LoseLifeUnlessDiscardEffect(5));
    }
}
