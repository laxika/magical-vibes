package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token that's a copy of the permanent referenced by the stack entry's targetPermanentId.
 * Used for "create a token that's a copy of that artifact/creature" triggered abilities
 * where the permanent to copy is determined at trigger time (e.g. Mirrorworks).
 * The token copies all copiable characteristics per CR 707.2.
 */
public record CreateTokenCopyOfTargetPermanentEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
