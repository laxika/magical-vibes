package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: if an opponent of this effect's controller would search a library, that player
 * searches only the top {@code count} cards of that library instead.
 *
 * <p>A passive rules-modifier read at search-initiation time by {@code LibrarySearchSupport}
 * (just like {@link CantSearchLibrariesEffect}); it never resolves off the stack, so it has no
 * {@code NormalEffectHandlerBean}. Aven Mindcensor uses {@code count = 4}.
 */
public record OpponentSearchesTopCardsInsteadEffect(int count) implements CardEffect {
}
