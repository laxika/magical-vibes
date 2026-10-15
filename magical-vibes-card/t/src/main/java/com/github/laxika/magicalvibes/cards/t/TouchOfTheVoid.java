package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "BFZ", collectorNumber = "134")
public class TouchOfTheVoid extends Card {

    public TouchOfTheVoid() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new Fixed(3), false, true));
    }
}
