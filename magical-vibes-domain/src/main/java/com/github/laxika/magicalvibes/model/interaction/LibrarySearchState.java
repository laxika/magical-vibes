package com.github.laxika.magicalvibes.model.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LibrarySearchState {

    private final UUID playerId;
    private final List<Card> cards;
    private final boolean reveals;
    private final boolean canFailToFind;
    private final UUID targetPlayerId;
    private final int remainingCount;
    private final List<Card> sourceCards;
    private final boolean reorderRemainingToBottom;
    private final boolean reorderRemainingToTop;
    private final boolean shuffleAfterSelection;
    private final String prompt;
    private final LibrarySearchDestination destination;
    private final Set<CardType> filterCardTypes;
    private final List<Card> accumulatedCards;

    public LibrarySearchState(UUID playerId, List<Card> cards, boolean reveals, boolean canFailToFind,
                              UUID targetPlayerId, int remainingCount, List<Card> sourceCards,
                              boolean reorderRemainingToBottom, boolean reorderRemainingToTop,
                              boolean shuffleAfterSelection,
                              String prompt, LibrarySearchDestination destination,
                              Set<CardType> filterCardTypes, List<Card> accumulatedCards) {
        this.playerId = playerId;
        this.cards = cards;
        this.reveals = reveals;
        this.canFailToFind = canFailToFind;
        this.targetPlayerId = targetPlayerId;
        this.remainingCount = remainingCount;
        this.sourceCards = sourceCards;
        this.reorderRemainingToBottom = reorderRemainingToBottom;
        this.reorderRemainingToTop = reorderRemainingToTop;
        this.shuffleAfterSelection = shuffleAfterSelection;
        this.prompt = prompt;
        this.destination = destination;
        this.filterCardTypes = filterCardTypes;
        this.accumulatedCards = accumulatedCards;
    }

    public UUID playerId() {
        return playerId;
    }

    public List<Card> cards() {
        return cards;
    }

    public boolean reveals() {
        return reveals;
    }

    public boolean canFailToFind() {
        return canFailToFind;
    }

    public UUID targetPlayerId() {
        return targetPlayerId;
    }

    public int remainingCount() {
        return remainingCount;
    }

    public List<Card> sourceCards() {
        return sourceCards;
    }

    public boolean reorderRemainingToBottom() {
        return reorderRemainingToBottom;
    }

    public boolean reorderRemainingToTop() {
        return reorderRemainingToTop;
    }

    public boolean shuffleAfterSelection() {
        return shuffleAfterSelection;
    }

    public String prompt() {
        return prompt;
    }

    public LibrarySearchDestination destination() {
        return destination;
    }

    public Set<CardType> filterCardTypes() {
        return filterCardTypes;
    }

    public List<Card> accumulatedCards() {
        return accumulatedCards;
    }

    public LibrarySearchState deepCopy() {
        return new LibrarySearchState(
                playerId,
                cards != null ? new ArrayList<>(cards) : null,
                reveals, canFailToFind, targetPlayerId, remainingCount,
                sourceCards != null ? new ArrayList<>(sourceCards) : null,
                reorderRemainingToBottom, reorderRemainingToTop, shuffleAfterSelection, prompt, destination,
                filterCardTypes != null ? new HashSet<>(filterCardTypes) : null,
                accumulatedCards != null ? new ArrayList<>(accumulatedCards) : null
        );
    }
}
