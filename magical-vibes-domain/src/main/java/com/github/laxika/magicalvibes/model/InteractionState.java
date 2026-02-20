package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InteractionState {

    public AwaitingInput awaitingInput;
    public InteractionContext context;
    public UUID awaitingCardChoicePlayerId;
    public Set<Integer> awaitingCardChoiceValidIndices;
    public UUID awaitingPermanentChoicePlayerId;
    public Set<UUID> awaitingPermanentChoiceValidIds;
    public Card pendingAuraCard;
    public PermanentChoiceContext permanentChoiceContext;
    public UUID pendingCardChoiceTargetPermanentId;
    public UUID awaitingGraveyardChoicePlayerId;
    public Set<Integer> awaitingGraveyardChoiceValidIndices;
    public GraveyardChoiceDestination graveyardChoiceDestination;
    public List<Card> graveyardChoiceCardPool;
    public UUID awaitingColorChoicePlayerId;
    public UUID awaitingColorChoicePermanentId;
    public UUID pendingColorChoiceETBTargetId;
    public ColorChoiceContext colorChoiceContext;
    public UUID awaitingMayAbilityPlayerId;
    public UUID awaitingMultiPermanentChoicePlayerId;
    public Set<UUID> awaitingMultiPermanentChoiceValidIds;
    public int awaitingMultiPermanentChoiceMaxCount;
    public UUID awaitingMultiGraveyardChoicePlayerId;
    public Set<UUID> awaitingMultiGraveyardChoiceValidCardIds;
    public int awaitingMultiGraveyardChoiceMaxCount;
    public UUID awaitingLibraryReorderPlayerId;
    public List<Card> awaitingLibraryReorderCards;
    public boolean awaitingLibraryReorderToBottom;
    public UUID awaitingLibrarySearchPlayerId;
    public List<Card> awaitingLibrarySearchCards;
    public boolean awaitingLibrarySearchReveals;
    public boolean awaitingLibrarySearchCanFailToFind;
    public UUID awaitingLibrarySearchTargetPlayerId;
    public int awaitingLibrarySearchRemainingCount;
    public int awaitingDiscardRemainingCount;
    public UUID awaitingRevealedHandChoiceTargetPlayerId;
    public int awaitingRevealedHandChoiceRemainingCount;
    public final List<Card> awaitingRevealedHandChosenCards = new ArrayList<>();
    public boolean awaitingRevealedHandChoiceDiscardMode;
    public UUID awaitingHandTopBottomPlayerId;
    public List<Card> awaitingHandTopBottomCards;
    public UUID awaitingLibraryRevealPlayerId;
    public List<Card> awaitingLibraryRevealAllCards;
    public Set<UUID> awaitingLibraryRevealValidCardIds;

    public void clearContext() {
        this.context = null;
    }

    public InteractionContext.CardChoice cardChoiceContext() {
        if (context instanceof InteractionContext.CardChoice cc) return cc;
        if (awaitingCardChoicePlayerId == null || awaitingCardChoiceValidIndices == null || awaitingInput == null) return null;
        return new InteractionContext.CardChoice(awaitingInput, awaitingCardChoicePlayerId, awaitingCardChoiceValidIndices,
                pendingCardChoiceTargetPermanentId);
    }

    public InteractionContext.PermanentChoice permanentChoiceContextView() {
        if (context instanceof InteractionContext.PermanentChoice pc) return pc;
        if (awaitingPermanentChoicePlayerId == null || awaitingPermanentChoiceValidIds == null) return null;
        return new InteractionContext.PermanentChoice(awaitingPermanentChoicePlayerId, awaitingPermanentChoiceValidIds, permanentChoiceContext);
    }

    public InteractionContext.GraveyardChoice graveyardChoiceContext() {
        if (context instanceof InteractionContext.GraveyardChoice gc) return gc;
        if (awaitingGraveyardChoicePlayerId == null || awaitingGraveyardChoiceValidIndices == null) return null;
        return new InteractionContext.GraveyardChoice(awaitingGraveyardChoicePlayerId, awaitingGraveyardChoiceValidIndices,
                graveyardChoiceDestination, graveyardChoiceCardPool);
    }

    public InteractionContext.ColorChoice colorChoiceContextView() {
        if (context instanceof InteractionContext.ColorChoice cc) return cc;
        if (awaitingColorChoicePlayerId == null && colorChoiceContext == null) return null;
        return new InteractionContext.ColorChoice(awaitingColorChoicePlayerId, awaitingColorChoicePermanentId,
                pendingColorChoiceETBTargetId, colorChoiceContext);
    }

    public InteractionContext.MayAbilityChoice mayAbilityChoiceContext() {
        if (context instanceof InteractionContext.MayAbilityChoice mc) return mc;
        if (awaitingMayAbilityPlayerId == null) return null;
        return new InteractionContext.MayAbilityChoice(awaitingMayAbilityPlayerId, "");
    }

    public InteractionContext.MultiPermanentChoice multiPermanentChoiceContext() {
        if (context instanceof InteractionContext.MultiPermanentChoice mpc) return mpc;
        if (awaitingMultiPermanentChoicePlayerId == null || awaitingMultiPermanentChoiceValidIds == null) return null;
        return new InteractionContext.MultiPermanentChoice(awaitingMultiPermanentChoicePlayerId,
                awaitingMultiPermanentChoiceValidIds, awaitingMultiPermanentChoiceMaxCount);
    }

    public InteractionContext.MultiGraveyardChoice multiGraveyardChoiceContext() {
        if (context instanceof InteractionContext.MultiGraveyardChoice mgc) return mgc;
        if (awaitingMultiGraveyardChoicePlayerId == null || awaitingMultiGraveyardChoiceValidCardIds == null) return null;
        return new InteractionContext.MultiGraveyardChoice(awaitingMultiGraveyardChoicePlayerId,
                awaitingMultiGraveyardChoiceValidCardIds, awaitingMultiGraveyardChoiceMaxCount);
    }

    public InteractionContext.LibraryReorder libraryReorderContext() {
        if (context instanceof InteractionContext.LibraryReorder lr) return lr;
        if (awaitingLibraryReorderPlayerId == null || awaitingLibraryReorderCards == null) return null;
        return new InteractionContext.LibraryReorder(awaitingLibraryReorderPlayerId, awaitingLibraryReorderCards,
                awaitingLibraryReorderToBottom);
    }

    public InteractionContext.LibrarySearch librarySearchContext() {
        if (context instanceof InteractionContext.LibrarySearch ls) return ls;
        if (awaitingLibrarySearchPlayerId == null || awaitingLibrarySearchCards == null) return null;
        return new InteractionContext.LibrarySearch(awaitingLibrarySearchPlayerId, awaitingLibrarySearchCards,
                awaitingLibrarySearchReveals, awaitingLibrarySearchCanFailToFind,
                awaitingLibrarySearchTargetPlayerId, awaitingLibrarySearchRemainingCount);
    }

    public InteractionContext.LibraryRevealChoice libraryRevealChoiceContext() {
        if (context instanceof InteractionContext.LibraryRevealChoice lrc) return lrc;
        if (awaitingLibraryRevealPlayerId == null || awaitingLibraryRevealAllCards == null || awaitingLibraryRevealValidCardIds == null) return null;
        return new InteractionContext.LibraryRevealChoice(awaitingLibraryRevealPlayerId,
                awaitingLibraryRevealAllCards, awaitingLibraryRevealValidCardIds);
    }

    public InteractionContext.HandTopBottomChoice handTopBottomChoiceContext() {
        if (context instanceof InteractionContext.HandTopBottomChoice htbc) return htbc;
        if (awaitingHandTopBottomPlayerId == null || awaitingHandTopBottomCards == null) return null;
        return new InteractionContext.HandTopBottomChoice(awaitingHandTopBottomPlayerId, awaitingHandTopBottomCards);
    }

    public InteractionContext.RevealedHandChoice revealedHandChoiceContext() {
        if (context instanceof InteractionContext.RevealedHandChoice rhc) return rhc;
        if (awaitingCardChoicePlayerId == null || awaitingCardChoiceValidIndices == null || awaitingRevealedHandChoiceTargetPlayerId == null) return null;
        return new InteractionContext.RevealedHandChoice(awaitingCardChoicePlayerId, awaitingRevealedHandChoiceTargetPlayerId,
                awaitingCardChoiceValidIndices, awaitingRevealedHandChoiceRemainingCount,
                awaitingRevealedHandChoiceDiscardMode, awaitingRevealedHandChosenCards);
    }
}
