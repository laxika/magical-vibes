package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToTargetsThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "86")
public class ComeBackWrong extends Card {

    public ComeBackWrong() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new DestroyUpToTargetsThenReturnFromGraveyardEffect(true));
    }
}
