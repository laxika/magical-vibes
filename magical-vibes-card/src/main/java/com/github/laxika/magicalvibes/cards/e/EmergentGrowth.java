package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleThisTurnEffect;

@CardRegistration(set = "XLN", collectorNumber = "188")
public class EmergentGrowth extends Card {

    public EmergentGrowth() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(5, 5));
        addEffect(EffectSlot.SPELL, new MustBeBlockedIfAbleThisTurnEffect());
    }
}
