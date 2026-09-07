package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "DIS", collectorNumber = "59")
public class CacklingFlames extends Card {

    public CacklingFlames() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControllerHandEmpty(),
                new DealDamageToAnyTargetEffect(3),
                new DealDamageToAnyTargetEffect(5)
        ));
    }
}
