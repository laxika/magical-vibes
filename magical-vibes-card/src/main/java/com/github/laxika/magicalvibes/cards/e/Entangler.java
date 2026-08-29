package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "7")
public class Entangler extends Card {

    public Entangler() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
    }
}
