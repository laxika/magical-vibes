package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player (in turn order) creates tokens described by {@code token} under their own control.
 * The wrapped {@link CreateTokenEffect}'s amount is re-evaluated relative to each creating player,
 * so a {@code CountScope.CONTROLLER} count reads that player's own board (e.g. Waiting in the Weeds:
 * "each player creates a 1/1 Cat for each untapped Forest they control").
 *
 * @param token                     blueprint for the token every player creates
 * @param recordAsCreatedWithSource when true, every created token id is registered under the source
 *                                  permanent in {@code GameData.sourceCreatedTokens}, so a paired
 *                                  {@link DestroyTokensCreatedWithSourceEffect} can find them again
 *                                  ("tokens created with this enchantment"; Tombstone Stairwell)
 */
public record EachPlayerCreatesTokenEffect(CreateTokenEffect token, boolean recordAsCreatedWithSource)
        implements CardEffect {

    /** Plain "each player creates …" with no created-with bookkeeping. */
    public EachPlayerCreatesTokenEffect(CreateTokenEffect token) {
        this(token, false);
    }
}
