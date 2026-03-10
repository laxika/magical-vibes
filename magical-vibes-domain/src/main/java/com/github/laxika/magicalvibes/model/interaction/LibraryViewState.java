package com.github.laxika.magicalvibes.model.interaction;

import com.github.laxika.magicalvibes.model.Card;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Groups library reorder, library reveal, and hand top/bottom interaction state.
 */
public class LibraryViewState {

    // Library reorder
    private UUID reorderPlayerId;
    private List<Card> reorderCards;
    private boolean reorderToBottom;

    // Library reveal choice
    private UUID revealPlayerId;
    private List<Card> revealAllCards;
    private Set<UUID> revealValidCardIds;

    // Scry
    private UUID scryPlayerId;
    private List<Card> scryCards;

    // Hand top/bottom choice
    private UUID handTopBottomPlayerId;
    private List<Card> handTopBottomCards;

    public LibraryViewState() {
    }

    // --- Library reorder ---

    public void setReorder(UUID playerId, List<Card> cards, boolean toBottom) {
        this.reorderPlayerId = playerId;
        this.reorderCards = cards;
        this.reorderToBottom = toBottom;
    }

    public void clearReorder() {
        this.reorderPlayerId = null;
        this.reorderCards = null;
        this.reorderToBottom = false;
    }

    public UUID reorderPlayerId() {
        return reorderPlayerId;
    }

    public List<Card> reorderCards() {
        return reorderCards;
    }

    public boolean reorderToBottom() {
        return reorderToBottom;
    }

    // --- Scry ---

    public void setScry(UUID playerId, List<Card> cards) {
        this.scryPlayerId = playerId;
        this.scryCards = cards;
    }

    public void clearScry() {
        this.scryPlayerId = null;
        this.scryCards = null;
    }

    public UUID scryPlayerId() {
        return scryPlayerId;
    }

    public List<Card> scryCards() {
        return scryCards;
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

    // --- Hand top/bottom choice ---

    public void setHandTopBottom(UUID playerId, List<Card> cards) {
        this.handTopBottomPlayerId = playerId;
        this.handTopBottomCards = cards;
    }

    public void clearHandTopBottom() {
        this.handTopBottomPlayerId = null;
        this.handTopBottomCards = null;
    }

    public UUID handTopBottomPlayerId() {
        return handTopBottomPlayerId;
    }

    public List<Card> handTopBottomCards() {
        return handTopBottomCards;
    }

    public LibraryViewState deepCopy() {
        LibraryViewState copy = new LibraryViewState();
        copy.reorderPlayerId = this.reorderPlayerId;
        copy.reorderCards = this.reorderCards != null ? new ArrayList<>(this.reorderCards) : null;
        copy.reorderToBottom = this.reorderToBottom;
        copy.scryPlayerId = this.scryPlayerId;
        copy.scryCards = this.scryCards != null ? new ArrayList<>(this.scryCards) : null;
        copy.revealPlayerId = this.revealPlayerId;
        copy.revealAllCards = this.revealAllCards != null ? new ArrayList<>(this.revealAllCards) : null;
        copy.revealValidCardIds = this.revealValidCardIds != null ? new HashSet<>(this.revealValidCardIds) : null;
        copy.handTopBottomPlayerId = this.handTopBottomPlayerId;
        copy.handTopBottomCards = this.handTopBottomCards != null ? new ArrayList<>(this.handTopBottomCards) : null;
        return copy;
    }
}
