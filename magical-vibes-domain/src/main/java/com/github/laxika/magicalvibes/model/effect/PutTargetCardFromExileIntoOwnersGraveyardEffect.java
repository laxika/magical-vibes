package com.github.laxika.magicalvibes.model.effect;

/** Moves a targeted face-up exiled card to its owner's graveyard. */
public record PutTargetCardFromExileIntoOwnersGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exileCard());
    }
}
