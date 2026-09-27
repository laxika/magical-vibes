package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDrawsThenControllerDrawsEffect;

@CardRegistration(set = "MSC", collectorNumber = "127")
public class CutADeal extends Card {

    public CutADeal() {
        addEffect(EffectSlot.SPELL, new EachOpponentDrawsThenControllerDrawsEffect());
    }
}
