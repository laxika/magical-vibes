package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Returns the source card from its graveyard to the battlefield transformed under its
 * controller's control and attaches the resulting Aura to the targeted opposing permanent.
 */
public record ReturnSourceTransformedFromGraveyardAttachedToTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creatureOrPlaneswalker(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()));
    }
}
