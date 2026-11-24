package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostAmountPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "40")
public class MandateOfAbaddon extends Card {

    public MandateOfAbaddon() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(DestroyAllPermanentsEffect.readingTarget(
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtMostAmountPredicate(
                                        new Sum(new TargetPower(true), new Fixed(-1))))),
                        TargetPredicates.creature()));
    }
}
