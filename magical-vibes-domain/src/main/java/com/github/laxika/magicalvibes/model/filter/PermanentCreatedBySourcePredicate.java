package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a token the source permanent created and tracks ({@code GameData.sourceCreatedTokens}): "the token" of an
 * ability linked to the one that created it (Dance of Many's "when the token leaves the battlefield").
 */
public record PermanentCreatedBySourcePredicate() implements PermanentPredicate {
}
