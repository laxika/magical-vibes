package com.github.laxika.magicalvibes.model.effect;

/** Copies an instant or sorcery card from a target player's hand for a possible free cast. */
public record CopyInstantOrSorceryFromTargetHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
