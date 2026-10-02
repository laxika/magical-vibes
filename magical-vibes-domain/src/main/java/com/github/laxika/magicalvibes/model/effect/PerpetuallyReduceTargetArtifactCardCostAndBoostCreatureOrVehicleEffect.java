package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Perpetually reduces a target artifact card's generic cost and boosts it if it is a creature or Vehicle. */
public record PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect(
        int costReduction, int powerBoost, int toughnessBoost) implements CardEffect {

    public PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect {
        if (costReduction <= 0) {
            throw new IllegalArgumentException("costReduction must be positive");
        }
    }

    public PerpetuallyReduceTargetArtifactCardCostAndBoostCreatureOrVehicleEffect() {
        this(1, 2, 2);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardTypePredicate(CardType.ARTIFACT), GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
