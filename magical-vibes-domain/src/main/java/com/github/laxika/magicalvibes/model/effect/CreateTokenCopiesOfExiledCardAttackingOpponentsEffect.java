package com.github.laxika.magicalvibes.model.effect;

/** Creates one tapped copy of the card exiled by the source for each opponent. */
public record CreateTokenCopiesOfExiledCardAttackingOpponentsEffect(boolean grantHaste) implements CardEffect {

    public CreateTokenCopiesOfExiledCardAttackingOpponentsEffect() {
        this(false);
    }
}
