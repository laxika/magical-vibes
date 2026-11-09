package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller puts one card they own that is exiled "with" the source permanent (tracked via
 * {@code GameData.exiledCards} / {@code sourcePermanentId}) into their hand. When several such cards
 * exist the controller chooses which one. Wrap in {@link MayEffect} for "you may" wording.
 *
 * <p>{@code sameNameAsTriggeringCard} narrows the pool to cards with the name of the card the trigger event was about
 * ("whenever you play a card with the same name as one of the exiled cards, you may put one of those cards with that
 * name into its owner's hand" — Search the City): the stack entry's triggering spell, or the land played, read as the
 * ability resolves (the spell as it last existed on the stack once it has left, CR 608.2h). Because every candidate
 * then shares a name, the name-filtered form never prompts for a choice.
 *
 * <p>Companion to {@link SearchLibraryForCardsToExileWithSourceEffect}. Used by Endless Horizons's
 * upkeep trigger.
 */
public record PutCardExiledWithSourceIntoHandEffect(CardPredicate filter, boolean toOwnersHand,
                                                   boolean sameNameAsTriggeringCard) implements CardEffect {

    public PutCardExiledWithSourceIntoHandEffect(CardPredicate filter, boolean toOwnersHand) {
        this(filter, toOwnersHand, false);
    }

    /** The source's controller chooses a resource, which returns to its owner. */
    public static PutCardExiledWithSourceIntoHandEffect returningToOwner() {
        return new PutCardExiledWithSourceIntoHandEffect(null, true);
    }

    /**
     * "Put one of those cards with that name into its owner's hand": a card exiled with the source with the name of the
     * card the trigger event was about.
     */
    public static PutCardExiledWithSourceIntoHandEffect withTriggeringCardNameToOwnersHand() {
        return new PutCardExiledWithSourceIntoHandEffect(null, true, true);
    }

    /** "Put a card exiled with this permanent into your hand" — no name restriction. */
    public PutCardExiledWithSourceIntoHandEffect() {
        this(null, false);
    }

    /** "Put a matching card exiled with this permanent into your hand." */
    public PutCardExiledWithSourceIntoHandEffect(CardPredicate filter) {
        this(filter, false);
    }
}
