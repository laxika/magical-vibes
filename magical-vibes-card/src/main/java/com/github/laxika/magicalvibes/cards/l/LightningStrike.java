package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "XLN", collectorNumber = "149")
@CardRegistration(set = "M15", collectorNumber = "155")
@CardRegistration(set = "M19", collectorNumber = "152")
@CardRegistration(set = "THS", collectorNumber = "127")
public class LightningStrike extends Card {

    public LightningStrike() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
