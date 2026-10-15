package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "125")
public class BumiBash extends Card {

    public BumiBash() {
        TargetFilter creature = TargetFilters.creature();
        PermanentPredicate landCreatureOrNonbasicLand = new PermanentAnyOfPredicate(List.of(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                ))
        ));
        TargetFilter landCreatureOrNonbasicLandTarget = new PermanentPredicateTargetFilter(
                landCreatureOrNonbasicLand,
                "Target must be a land creature or nonbasic land."
        );

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Bumi Bash deals damage equal to the number of lands you control to target creature",
                        new DealDamageToTargetCreatureEffect(
                                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)),
                        creature
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target land creature or nonbasic land",
                        new DestroyTargetPermanentEffect(),
                        landCreatureOrNonbasicLandTarget
                )
        )));
    }
}
