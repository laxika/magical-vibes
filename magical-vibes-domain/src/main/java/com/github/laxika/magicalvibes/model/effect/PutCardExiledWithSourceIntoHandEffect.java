package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller puts one card they own that is exiled "with" the source permanent (tracked via
 * {@code GameData.exiledCards} / {@code sourcePermanentId}) into their hand. When several such cards
 * exist the controller chooses which one. Wrap in {@link MayEffect} for "you may" wording.
 *
 * <p>Companion to {@link SearchLibraryForCardsToExileWithSourceEffect}. Used by Endless Horizons's
 * upkeep trigger.
 */
public record PutCardExiledWithSourceIntoHandEffect() implements CardEffect {
}
