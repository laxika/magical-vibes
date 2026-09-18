package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RequireFlagbearerTargetEffect;

@CardRegistration(set = "APC", collectorNumber = "18")
public class StandardBearer extends Card {

    public StandardBearer() {
        addEffect(EffectSlot.STATIC, new RequireFlagbearerTargetEffect());
    }
}
