package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "RAV", collectorNumber = "31")
public class SuppressionField extends Card {

    public SuppressionField() {
        addEffect(EffectSlot.STATIC, new IncreaseActivatedAbilityCostEffect(
                new PermanentTruePredicate(), 2, false, true));
    }
}
