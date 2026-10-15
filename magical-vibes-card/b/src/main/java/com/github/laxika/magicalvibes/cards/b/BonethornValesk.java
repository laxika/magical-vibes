package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SCG", collectorNumber = "82")
public class BonethornValesk extends Card {

    public BonethornValesk() {
        addEffect(EffectSlot.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP,
                new DealDamageToAnyTargetEffect(1));
    }
}
