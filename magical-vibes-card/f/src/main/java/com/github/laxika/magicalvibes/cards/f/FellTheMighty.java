package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastAmountPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1778")
@CardRegistration(set = "OTP", collectorNumber = "1")
@CardRegistration(set = "C14", collectorNumber = "7")
@CardRegistration(set = "MOC", collectorNumber = "185")
@CardRegistration(set = "40K", collectorNumber = "186")
@CardRegistration(set = "NCC", collectorNumber = "200")
@CardRegistration(set = "LTC", collectorNumber = "167")
@CardRegistration(set = "MKC", collectorNumber = "65")
@CardRegistration(set = "VOC", collectorNumber = "85")
@CardRegistration(set = "C17", collectorNumber = "60")
public class FellTheMighty extends Card {

    public FellTheMighty() {
        // Destroy all creatures with power greater than target creature's power.
        target(TargetFilters.creature()).addSpellEffect(DestroyTargetPermanentEffect.readingTarget(
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastAmountPredicate(
                                new Sum(new TargetPower(true), new Fixed(1))))),
                TargetPredicates.creature()));
    }
}
