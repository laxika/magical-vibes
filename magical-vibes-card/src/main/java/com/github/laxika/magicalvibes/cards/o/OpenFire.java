package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "HOU", collectorNumber = "105")
public class OpenFire extends Card {

    public OpenFire() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
