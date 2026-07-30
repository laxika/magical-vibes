package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "4")
public class AysenCrusader extends Card {

    public AysenCrusader() {
        // 2 plus the number of Soldiers and Warriors you control (it is a Knight, so it never counts itself).
        DynamicAmount powerToughness = new Sum(
                new Fixed(2),
                new PermanentCount(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SOLDIER, CardSubtype.WARRIOR)),
                        CountScope.CONTROLLER));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(powerToughness, powerToughness));
    }
}
