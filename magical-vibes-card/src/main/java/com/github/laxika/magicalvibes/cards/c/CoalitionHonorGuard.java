package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequireFlagbearerTargetEffect;

@CardRegistration(set = "APC", collectorNumber = "3")
@CardRegistration(set = "EMA", collectorNumber = "6")
public class CoalitionHonorGuard extends Card {
    public CoalitionHonorGuard() {
        addEffect(EffectSlot.STATIC, new RequireFlagbearerTargetEffect());
    }
}
