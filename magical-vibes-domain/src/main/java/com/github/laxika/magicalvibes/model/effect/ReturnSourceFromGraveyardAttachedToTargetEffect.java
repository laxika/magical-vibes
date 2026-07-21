package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/**
 * Return the source card from its owner's graveyard to the battlefield attached to the targeted
 * creature (e.g. Gryff's Boon). The creature is chosen as a true target at activation; if it is
 * illegal on resolution the ability fizzles and the card stays in the graveyard.
 */
public record ReturnSourceFromGraveyardAttachedToTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE, new PermanentIsCreaturePredicate());
    }
}
