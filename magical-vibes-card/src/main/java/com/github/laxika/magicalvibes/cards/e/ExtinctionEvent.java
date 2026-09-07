package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesOfChosenManaValueParityEffect;

@CardRegistration(set = "IKO", collectorNumber = "88")
public class ExtinctionEvent extends Card {

    public ExtinctionEvent() {
        addEffect(EffectSlot.SPELL, new ExileAllCreaturesOfChosenManaValueParityEffect());
    }
}
