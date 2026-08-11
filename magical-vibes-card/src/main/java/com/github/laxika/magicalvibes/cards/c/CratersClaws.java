package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "KTK", collectorNumber = "106")
public class CratersClaws extends Card {

    public CratersClaws() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new Sum(
                new XValue(),
                new FixedIfCondition(new ControlsPermanent(new PermanentPowerAtLeastPredicate(4)), 2, 0))));
    }
}
