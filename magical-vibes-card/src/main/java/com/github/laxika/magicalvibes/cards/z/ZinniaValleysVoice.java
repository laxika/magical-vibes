package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantOffspringToCreatureSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBasePowerEqualsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "4")
@CardRegistration(set = "BLC", collectorNumber = "104")
public class ZinniaValleysVoice extends Card {

    public ZinniaValleysVoice() {
        PermanentCount otherBasePowerOneCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentBasePowerEqualsPredicate(1))),
                CountScope.CONTROLLER, true);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                otherBasePowerOneCreatures, new Fixed(0), GrantScope.SELF));
        addEffect(EffectSlot.STATIC, new GrantOffspringToCreatureSpellsEffect("{2}"));
    }
}
