package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DMU", collectorNumber = "117")
public class ChaoticTransformation extends Card {

    public ChaoticTransformation() {
        setAllowSharedTargets(true);
        target(new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(),
                "Target must be an artifact"), 0, 1);
        target(new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                "Target must be a creature"), 0, 1);
        target(new PermanentPredicateTargetFilter(new PermanentIsEnchantmentPredicate(),
                "Target must be an enchantment"), 0, 1);
        target(new PermanentPredicateTargetFilter(new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"), 0, 1);
        target(new PermanentPredicateTargetFilter(new PermanentIsLandPredicate(),
                "Target must be a land"), 0, 1);
        addEffect(EffectSlot.SPELL, new ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect());
        setMultiTargetConstraint(
                MultiTargetConstraint.AT_MOST_ONE_ARTIFACT_ONE_CREATURE_ONE_ENCHANTMENT_ONE_PLANESWALKER_AND_ONE_LAND);
    }
}
