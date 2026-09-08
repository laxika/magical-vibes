package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "171")
public class MarkovRetribution extends Card {

    public MarkovRetribution() {
        var vampireYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)
                )),
                "Target must be a Vampire you control"
        );

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+0 until end of turn",
                        new BoostAllOwnCreaturesEffect(1, 0)
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Target Vampire you control deals damage equal to its power to another target creature",
                        List.of(new TargetDealsPowerDamageToTargetEffect()),
                        List.of(vampireYouControl, TargetFilters.creature())
                )
        )));
    }
}
