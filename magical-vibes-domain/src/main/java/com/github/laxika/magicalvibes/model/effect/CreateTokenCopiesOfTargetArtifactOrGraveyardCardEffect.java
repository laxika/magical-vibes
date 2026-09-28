package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/** Creates the specified number of token copies of a target artifact permanent or graveyard card. */
public record CreateTokenCopiesOfTargetArtifactOrGraveyardCardEffect(int amount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.permanents(new PermanentIsArtifactPredicate()),
                TargetPredicates.graveyardCards(
                        new CardTypePredicate(CardType.ARTIFACT), GraveyardSearchScope.ALL_GRAVEYARDS)));
    }
}
