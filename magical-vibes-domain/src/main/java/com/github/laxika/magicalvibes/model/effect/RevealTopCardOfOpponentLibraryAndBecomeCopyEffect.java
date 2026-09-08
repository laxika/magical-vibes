package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the active opponent's top library card and, if it is a creature card, mills it and makes
 * the source permanent a copy of it permanently.
 */
public record RevealTopCardOfOpponentLibraryAndBecomeCopyEffect() implements CardEffect {
}
