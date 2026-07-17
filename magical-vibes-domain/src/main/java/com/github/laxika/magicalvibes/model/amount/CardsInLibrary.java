package com.github.laxika.magicalvibes.model.amount;

/** The number of cards in the library/libraries in scope (e.g. Invincible Hymn's "cards in your library"). */
public record CardsInLibrary(CountScope scope) implements DynamicAmount {
}
