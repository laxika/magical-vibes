package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

/** Makes the source permanent a copy of a target artifact, non-Aura enchantment, or land. */
public record BecomeCopyOfTargetPermanentUntilYourNextTurnEffect(
        String nameOverride,
        Integer powerOverride,
        Integer toughnessOverride,
        Set<CardSubtype> additionalSubtypesOverride,
        Set<CardType> additionalTypesOverride,
        Set<CardSupertype> additionalSupertypesOverride,
        Set<Keyword> additionalKeywordsOverride
) implements CardEffect {

    public BecomeCopyOfTargetPermanentUntilYourNextTurnEffect {
        additionalSubtypesOverride = additionalSubtypesOverride == null
                ? Set.of() : Set.copyOf(additionalSubtypesOverride);
        additionalTypesOverride = additionalTypesOverride == null
                ? Set.of() : Set.copyOf(additionalTypesOverride);
        additionalSupertypesOverride = additionalSupertypesOverride == null
                ? Set.of() : Set.copyOf(additionalSupertypesOverride);
        additionalKeywordsOverride = additionalKeywordsOverride == null
                ? Set.of() : Set.copyOf(additionalKeywordsOverride);
    }

    private static PermanentPredicate targetPredicate() {
        return new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA))
                )),
                new PermanentIsLandPredicate()
        ));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), targetPredicate());
    }
}
