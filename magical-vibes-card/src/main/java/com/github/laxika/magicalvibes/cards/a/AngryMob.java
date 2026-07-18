package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DuringControllerTurn;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "5ED", collectorNumber = "4")
@CardRegistration(set = "4ED", collectorNumber = "3")
public class AngryMob extends Card {

    public AngryMob() {
        // During your turn: 2 + Swamps your opponents control. On other turns: a flat 2.
        DynamicAmount powerToughness = new Sum(
                new Fixed(2),
                new DuringControllerTurn(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.OPPONENTS)));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(powerToughness, powerToughness));
    }
}
