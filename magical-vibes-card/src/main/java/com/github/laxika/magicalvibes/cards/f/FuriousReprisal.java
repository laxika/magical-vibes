package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachTargetEffect;

@CardRegistration(set = "KLD", collectorNumber = "115")
public class FuriousReprisal extends Card {

    public FuriousReprisal() {
        target(2, 2).addEffect(EffectSlot.SPELL, new DealDamageToEachTargetEffect(new Fixed(2)));
    }
}
