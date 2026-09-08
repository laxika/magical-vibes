package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "CSP", collectorNumber = "87")
public class KarplusanWolverine extends Card {

    public KarplusanWolverine() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new MayEffect(new DealDamageToAnyTargetEffect(1),
                        "Have Karplusan Wolverine deal 1 damage to any target?"));
    }
}
