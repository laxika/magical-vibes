package com.github.laxika.magicalvibes.model.interaction;

import com.github.laxika.magicalvibes.model.Card;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Holds library reveal interaction state.
 * (Scry, hand-top-bottom, and library reorder have migrated to {@code PendingInteraction}
 * + the InteractionHandlerRegistry.)
 */
public class LibraryViewState {

    // Library reveal choice
    private UUID revealPlayerId;
    private List<Card> revealAllCards;
    private Set<UUID> revealValidCardIds;

    public LibraryViewState() {
    }

    // --- Library reveal choice ---

    public void setReveal(UUID playerId, List<Card> allCards, Set<UUID> validCardIds) {
        this.revealPlayerId = playerId;
        this.revealAllCards = allCards;
        this.revealValidCardIds = validCardIds;
    }

    public void clearReveal() {
        this.revealPlayerId = null;
        this.revealAllCards = null;
        this.revealValidCardIds = null;
    }

    public UUID revealPlayerId() {
        return revealPlayerId;
    }

    public List<Card> revealAllCards() {
        return revealAllCards;
    }

    public Set<UUID> revealValidCardIds() {
        return revealValidCardIds;
    }

    public LibraryViewState deepCopy() {
        LibraryViewState copy = new LibraryViewState();
        copy.revealPlayerId = this.revealPlayerId;
        copy.revealAllCards = this.revealAllCards != null ? new ArrayList<>(this.revealAllCards) : null;
        copy.revealValidCardIds = this.revealValidCardIds != null ? new HashSet<>(this.revealValidCardIds) : null;
        return copy;
    }
}
