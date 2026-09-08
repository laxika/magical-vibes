package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "135")
public class HulkSmash extends Card {

    public HulkSmash() {
        var noncreatureArtifact = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                )),
                "Target must be a noncreature artifact");

        addEffect(EffectSlot.SPELL, new TeamworkCost(4));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target noncreature artifact",
                        new DestroyTargetPermanentEffect(),
                        noncreatureArtifact),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control deals damage equal to its power to target creature an opponent controls",
                        List.<CardEffect>of(new ConditionalReplacementEffect(
                                new TeamworkCostPaid(),
                                new TargetDealsPowerDamageToTargetEffect(),
                                new TargetDealsPowerDamageToTargetEffect(1, 2))),
                        List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()))
        ), false, 1, 2, true, false, new TeamworkCostPaid()));
    }
}
