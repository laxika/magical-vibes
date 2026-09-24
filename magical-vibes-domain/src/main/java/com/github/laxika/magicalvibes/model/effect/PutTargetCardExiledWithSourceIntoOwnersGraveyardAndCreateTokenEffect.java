package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Puts a targeted card exiled with the source into its owner's graveyard, then creates a token based on its type. */
public record PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect(
        CreateTokenEffect landToken,
        CreateTokenEffect nonlandToken
) implements CardEffect {

    public PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect {
        Objects.requireNonNull(landToken, "landToken");
        Objects.requireNonNull(nonlandToken, "nonlandToken");
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exileCard());
    }
}
