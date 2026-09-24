package com.github.laxika.magicalvibes.model.effect;

/** Creates token copies of the permanent sacrificed to pay an activated ability's cost. */
public record CreateTokenCopyOfSacrificedPermanentEffect(int amount) implements CardEffect {

    public CreateTokenCopyOfSacrificedPermanentEffect() {
        this(1);
    }
}
