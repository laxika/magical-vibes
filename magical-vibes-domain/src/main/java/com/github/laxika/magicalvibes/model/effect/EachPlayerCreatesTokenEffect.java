package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player {@code players} names (every player by default, or each opponent; in APNAP order) creates tokens
 * described by {@code token} under their own control.
 * The wrapped {@link CreateTokenEffect}'s amount is re-evaluated relative to each creating player,
 * so a {@code CountScope.CONTROLLER} count reads that player's own board (e.g. Waiting in the Weeds:
 * "each player creates a 1/1 Cat for each untapped Forest they control").
 *
 * @param token                     blueprint for the token every player creates
 * @param recordAsCreatedWithSource when true, every created token id is registered under the source
 *                                  permanent in {@code GameData.sourceCreatedTokens}, so a paired
 *                                  {@link DestroyTokensCreatedWithSourceEffect} can find them again
 *                                  ("tokens created with this enchantment"; Tombstone Stairwell)
 * @param players                   who creates tokens; never a target
 */
public record EachPlayerCreatesTokenEffect(CreateTokenEffect token, boolean recordAsCreatedWithSource,
                                           PlayerSubject players)
        implements CardEffect {

    public EachPlayerCreatesTokenEffect {
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who create tokens are not targeted: " + players);
        }
    }

    /** "Each player creates …", recorded as created with the source or not. */
    public EachPlayerCreatesTokenEffect(CreateTokenEffect token, boolean recordAsCreatedWithSource) {
        this(token, recordAsCreatedWithSource, Subjects.eachPlayer());
    }

    /** {@code players} each create the token. */
    public EachPlayerCreatesTokenEffect(CreateTokenEffect token, PlayerSubject players) {
        this(token, false, players);
    }

    /** Plain "each player creates …" with no created-with bookkeeping. */
    public EachPlayerCreatesTokenEffect(CreateTokenEffect token) {
        this(token, false);
    }
}
