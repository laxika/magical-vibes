package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller puts one card they own that is exiled "with" the source permanent (tracked via
 * {@code GameData.exiledCards} / {@code sourcePermanentId}) into their hand. When several such cards
 * exist the controller chooses which one. Wrap in {@link MayEffect} for "you may" wording.
 *
 * <p>{@code requiredName} narrows the pool to cards with that exact name ("put one of those cards
 * with that name into its owner's hand" — Search the City); {@code null} means any card exiled with
 * the source. Because every candidate then shares a name, the name-filtered form never prompts for a
 * choice.
 *
 * <p>Companion to {@link SearchLibraryForCardsToExileWithSourceEffect}. Used by Endless Horizons's
 * upkeep trigger.
 */
public record PutCardExiledWithSourceIntoHandEffect(String requiredName) implements CardEffect {

    /** "Put a card exiled with this permanent into your hand" — no name restriction. */
    public PutCardExiledWithSourceIntoHandEffect() {
        this(null);
    }
}
