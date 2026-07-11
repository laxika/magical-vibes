package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "194")
public class ConeOfFlame extends Card {

    public ConeOfFlame() {
        target(3, 3)
                .addEffect(EffectSlot.SPELL, DealDividedDamageEffect.ordered(List.of(1, 2, 3)));
    }
}
