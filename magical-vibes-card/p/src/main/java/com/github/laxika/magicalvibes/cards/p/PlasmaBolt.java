package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "EOE", collectorNumber = "152")
public class PlasmaBolt extends Card {

    public PlasmaBolt() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(
                new FixedIfCondition(new VoidCondition(), 3, 2)));
    }
}
