package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "GRN", collectorNumber = "53")
public class SelectiveSnare extends Card {

    public SelectiveSnare() {
        targetX(TargetFilters.creature(), 100)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.targetCreaturesOfChosenType());
    }
}
