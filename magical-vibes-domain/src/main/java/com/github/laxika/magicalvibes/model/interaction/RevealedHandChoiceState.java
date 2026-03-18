package com.github.laxika.magicalvibes.model.interaction;

import com.github.laxika.magicalvibes.model.Card;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RevealedHandChoiceState {

    private UUID choosingPlayerId;
    private Set<Integer> validIndices;
    private UUID targetPlayerId;
    private int remainingCount;
    private final List<Card> chosenCards = new ArrayList<>();
    private boolean discardMode;
    private boolean exileMode;
    private int discardRemainingCount;

    public RevealedHandChoiceState(UUID choosingPlayerId, Set<Integer> validIndices,
                                   UUID targetPlayerId, int remainingCount,
                                   boolean discardMode, List<Card> initialChosenCards) {
        this(choosingPlayerId, validIndices, targetPlayerId, remainingCount, discardMode, false, initialChosenCards);
    }

    public RevealedHandChoiceState(UUID choosingPlayerId, Set<Integer> validIndices,
                                   UUID targetPlayerId, int remainingCount,
                                   boolean discardMode, boolean exileMode, List<Card> initialChosenCards) {
        this.choosingPlayerId = choosingPlayerId;
        this.validIndices = validIndices;
        this.targetPlayerId = targetPlayerId;
        this.remainingCount = remainingCount;
        this.discardMode = discardMode;
        this.exileMode = exileMode;
        if (initialChosenCards != null) {
            this.chosenCards.addAll(initialChosenCards);
        }
    }

    public UUID choosingPlayerId() {
        return choosingPlayerId;
    }

    public void setChoosingPlayerId(UUID choosingPlayerId) {
        this.choosingPlayerId = choosingPlayerId;
    }

    public Set<Integer> validIndices() {
        return validIndices;
    }

    public void setValidIndices(Set<Integer> validIndices) {
        this.validIndices = validIndices;
    }

    public UUID targetPlayerId() {
        return targetPlayerId;
    }

    public int remainingCount() {
        return remainingCount;
    }

    public int decrementRemainingCount() {
        if (this.remainingCount > 0) {
            this.remainingCount--;
        }
        return this.remainingCount;
    }

    public List<Card> chosenCardsSnapshot() {
        return new ArrayList<>(chosenCards);
    }

    public void addChosenCard(Card card) {
        this.chosenCards.add(card);
    }

    public boolean discardMode() {
        return discardMode;
    }

    public boolean exileMode() {
        return exileMode;
    }

    public int discardRemainingCount() {
        return discardRemainingCount;
    }

    public void setDiscardRemainingCount(int count) {
        this.discardRemainingCount = Math.max(count, 0);
    }

    public int decrementDiscardRemainingCount() {
        if (this.discardRemainingCount > 0) {
            this.discardRemainingCount--;
        }
        return this.discardRemainingCount;
    }

    public void clearProgress() {
        this.targetPlayerId = null;
        this.remainingCount = 0;
        this.discardMode = false;
        this.exileMode = false;
        this.chosenCards.clear();
    }

    public RevealedHandChoiceState deepCopy() {
        RevealedHandChoiceState copy = new RevealedHandChoiceState(
                choosingPlayerId,
                validIndices != null ? new HashSet<>(validIndices) : null,
                targetPlayerId,
                remainingCount,
                discardMode,
                exileMode,
                chosenCards
        );
        copy.discardRemainingCount = this.discardRemainingCount;
        return copy;
    }
}
