package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates token(s) that are copies of the source permanent (the permanent with this ability).
 * The token copies all copiable characteristics per CR 707.2.
 *
 * @param removeLegendary if true, the token is not legendary (removes LEGENDARY supertype)
 * @param amount          number of token copies to create
 */
public record CreateTokenCopyOfSourceEffect(boolean removeLegendary, int amount) implements CardEffect {

    /** Backward-compatible: single copy, keeps legendary status. */
    public CreateTokenCopyOfSourceEffect() {
        this(false, 1);
    }
}
