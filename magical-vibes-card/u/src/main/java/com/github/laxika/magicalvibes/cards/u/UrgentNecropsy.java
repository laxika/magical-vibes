package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "240")
@CardRegistration(set = "MKM", collectorNumber = "421")
public class UrgentNecropsy extends Card {

    public UrgentNecropsy() {
        addEffect(EffectSlot.SPELL, CollectEvidenceCost.forTargetManaValue());
        setAllowSharedTargets(true);

        target(TargetFilters.artifact(), 0, 1);
        target(TargetFilters.creature(), 0, 1);
        target(TargetFilters.enchantment(), 0, 1);
        target(new PermanentPredicateTargetFilter(new PermanentIsPlaneswalkerPredicate(),
                "Target must be a planeswalker"), 0, 1);
        addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
        setMultiTargetConstraint(
                MultiTargetConstraint.AT_MOST_ONE_ARTIFACT_ONE_CREATURE_ONE_ENCHANTMENT_AND_ONE_PLANESWALKER);
    }
}
