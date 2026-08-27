package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "240")
@CardRegistration(set = "MKM", collectorNumber = "421")
public class UrgentNecropsy extends Card {

    public UrgentNecropsy() {
        addEffect(EffectSlot.SPELL, CollectEvidenceCost.forTargetManaValue());
        setAllowSharedTargets(true);

        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                "Target must be an artifact, creature, enchantment, or planeswalker"), 0, 4)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
        setMultiTargetConstraint(
                MultiTargetConstraint.AT_MOST_ONE_ARTIFACT_ONE_CREATURE_ONE_ENCHANTMENT_AND_ONE_PLANESWALKER);
    }
}
