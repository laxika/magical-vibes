package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "117")
public class ChaoticTransformation extends Card {

    public ChaoticTransformation() {
        setAllowSharedTargets(true);
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsLandPredicate())),
                "Target must be an artifact, creature, enchantment, planeswalker, or land"), 0, 5)
                .addEffect(EffectSlot.SPELL, new ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect());
        setMultiTargetConstraint(
                MultiTargetConstraint.AT_MOST_ONE_ARTIFACT_ONE_CREATURE_ONE_ENCHANTMENT_ONE_PLANESWALKER_AND_ONE_LAND);
    }
}
