package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "DMU", collectorNumber = "12")
public class CleavingSkyrider extends Card {

    public CleavingSkyrider() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{R}"));

        targetWhenKicked(null, 0, 0, 1, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(), new DealDamageToAnyTargetEffect(
                        new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER))));
    }
}
