package com.github.laxika.magicalvibes.model.effect;

/** Exiles all creature cards from a target player's graveyard face down, then manifests them. */
public record ExileCreaturesFromTargetGraveyardThenManifestEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
