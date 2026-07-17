package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "277")
public class AnHavvaConstable extends Card {

    public AnHavvaConstable() {
        // Toughness is 1 plus the number of green creatures on the battlefield. The printed
        // "1+*" toughness loads with a base of 0, so the "1 plus" is supplied here alongside
        // the green-creature count. It counts itself while on the battlefield.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new Fixed(0),
                new Sum(List.of(
                        new Fixed(1),
                        new PermanentCount(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.GREEN))
                        )), CountScope.ANY_PLAYER)))));
    }
}
