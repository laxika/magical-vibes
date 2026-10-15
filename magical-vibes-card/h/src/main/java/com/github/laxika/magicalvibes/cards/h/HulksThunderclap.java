package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BeholdCostPaid;
import com.github.laxika.magicalvibes.model.effect.BeholdCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "725")
public class HulksThunderclap extends Card {

    public HulksThunderclap() {
        addEffect(EffectSlot.SPELL, BeholdCost.optional(CardSubtype.GAMMA));

        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "First target must be a creature you control"
        )).addEffect(EffectSlot.SPELL, new TargetDealsPowerDamageToTargetEffect());

        target(TargetFilters.creature());

        PermanentPredicate noncreatureArtifactOrEnchantment = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                ))
        ));
        target(new PermanentPredicateTargetFilter(
                noncreatureArtifactOrEnchantment,
                "Target must be a noncreature artifact or noncreature enchantment"
        ), 0, 1).addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new BeholdCostPaid(), new DestroyTargetPermanentEffect()));
    }
}
