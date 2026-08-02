package com.github.laxika.magicalvibes.cards.p;

import java.util.List;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

@CardRegistration(set = "TMP", collectorNumber = "195")
public class Pallimud extends Card {

    public Pallimud() {
        // "As this creature enters, choose an opponent" is implicit in the single-opponent model,
        // so the CDA simply counts tapped lands opponents control. Toughness stays a flat 3.
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(), new PermanentIsTappedPredicate())),
                        CountScope.OPPONENTS),
                new Fixed(3)));
    }
}
