package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/** Sacrifices the targeted artifact and returns the other targeted artifact card. */
public record SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanents(new PermanentIsArtifactPredicate()),
                TargetPredicates.graveyardCards(
                        new CardTypePredicate(CardType.ARTIFACT), GraveyardSearchScope.ALL_GRAVEYARDS)));
    }
}
