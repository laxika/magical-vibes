package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToEachTargetEffect;

@CardRegistration(set = "DOM", collectorNumber = "133")
public class JayasImmolatingInferno extends Card {

    public JayasImmolatingInferno() {
        target(1, 3)
                .addEffect(EffectSlot.SPELL, new DealXDamageToEachTargetEffect());
    }
}
