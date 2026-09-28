package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturePerpetuallyBecomesEnchantmentAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YDSK", collectorNumber = "5")
public class Housemeld extends Card {

    public Housemeld() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ExileTargetCreaturePerpetuallyBecomesEnchantmentAndReturnAtNextEndStepEffect());
    }
}
