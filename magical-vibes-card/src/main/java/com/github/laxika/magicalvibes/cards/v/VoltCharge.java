package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "NPH", collectorNumber = "100")
public class VoltCharge extends Card {

    public VoltCharge() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
        addEffect(EffectSlot.SPELL, new ProliferateEffect());
    }
}
