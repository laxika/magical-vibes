package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "EOE", collectorNumber = "147")
public class NebulaDragon extends Card {

    public NebulaDragon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(3));
    }
}
