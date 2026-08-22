package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "OTJ", collectorNumber = "132")
public class LonghornSharpshooter extends Card {

    public LonghornSharpshooter() {
        addEffect(EffectSlot.ON_SELF_BECOMES_PLOTTED, new DealDamageToAnyTargetEffect(2));
    }
}
