package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.interaction.CardChoiceState;
import com.github.laxika.magicalvibes.model.interaction.ColorChoiceState;
import com.github.laxika.magicalvibes.model.interaction.GraveyardChoiceState;
import com.github.laxika.magicalvibes.model.interaction.LibrarySearchState;
import com.github.laxika.magicalvibes.model.interaction.LibraryViewState;
import com.github.laxika.magicalvibes.model.interaction.MultiSelectionState;
import com.github.laxika.magicalvibes.model.interaction.PermanentChoiceState;
import com.github.laxika.magicalvibes.model.interaction.RevealedHandChoiceState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InteractionState {

    // --- Core state ---
    private AwaitingInput awaitingInput;
    private InteractionContext context;

    // --- Grouped sub-states ---
    private CardChoiceState cardChoice;
    private PermanentChoiceState permanentChoice;
    private GraveyardChoiceState graveyardChoice;
    private ColorChoiceState colorChoice;
    private LibrarySearchState librarySearch;
    private final LibraryViewState libraryView = new LibraryViewState();
    private RevealedHandChoiceState revealedHandChoice;
    private final MultiSelectionState multiSelection = new MultiSelectionState();

    // --- Independent fields (lifecycle not tied to a single begin/clear cycle) ---
    private PermanentChoiceContext permanentChoiceContext;
    private Card pendingAuraCard;
    private UUID pendingAuraOwnerId;
    private UUID pendingEquipmentAttachEquipmentId;
    private UUID pendingEquipmentAttachTargetId;
    private UUID awaitingMayAbilityPlayerId;

    /**
     * Creates a deep copy of this interaction state for AI simulation.
     */
    public InteractionState deepCopy() {
        InteractionState copy = new InteractionState();
        copy.awaitingInput = this.awaitingInput;
        copy.context = this.context;
        copy.cardChoice = this.cardChoice != null ? this.cardChoice.deepCopy() : null;
        copy.permanentChoice = this.permanentChoice != null ? this.permanentChoice.deepCopy() : null;
        copy.graveyardChoice = this.graveyardChoice != null ? this.graveyardChoice.deepCopy() : null;
        copy.colorChoice = this.colorChoice != null ? this.colorChoice.deepCopy() : null;
        copy.librarySearch = this.librarySearch != null ? this.librarySearch.deepCopy() : null;
        LibraryViewState lvCopy = this.libraryView.deepCopy();
        copy.libraryView.setReorder(lvCopy.reorderPlayerId(), lvCopy.reorderCards(), lvCopy.reorderToBottom());
        copy.libraryView.setScry(lvCopy.scryPlayerId(), lvCopy.scryCards());
        copy.libraryView.setReveal(lvCopy.revealPlayerId(), lvCopy.revealAllCards(), lvCopy.revealValidCardIds());
        copy.libraryView.setHandTopBottom(lvCopy.handTopBottomPlayerId(), lvCopy.handTopBottomCards());
        copy.revealedHandChoice = this.revealedHandChoice != null ? this.revealedHandChoice.deepCopy() : null;
        MultiSelectionState msCopy = this.multiSelection.deepCopy();
        copy.multiSelection.setMultiPermanent(msCopy.multiPermanentPlayerId(), msCopy.multiPermanentValidIds(), msCopy.multiPermanentMaxCount());
        copy.multiSelection.setMultiGraveyard(msCopy.multiGraveyardPlayerId(), msCopy.multiGraveyardValidCardIds(), msCopy.multiGraveyardMaxCount());
        copy.multiSelection.setMultiZoneExile(msCopy.multiZoneExilePlayerId(), msCopy.multiZoneExileValidCardIds(), msCopy.multiZoneExileMaxCount());
        copy.permanentChoiceContext = this.permanentChoiceContext;
        copy.pendingAuraCard = this.pendingAuraCard;
        copy.pendingAuraOwnerId = this.pendingAuraOwnerId;
        copy.pendingEquipmentAttachEquipmentId = this.pendingEquipmentAttachEquipmentId;
        copy.pendingEquipmentAttachTargetId = this.pendingEquipmentAttachTargetId;
        copy.awaitingMayAbilityPlayerId = this.awaitingMayAbilityPlayerId;
        return copy;
    }

    // ========================================================================
    // Core awaiting input
    // ========================================================================

    public boolean isAwaitingInput() {
        return this.awaitingInput != null;
    }

    public AwaitingInput awaitingInputType() {
        return this.awaitingInput;
    }

    public void setAwaitingInput(AwaitingInput inputType) {
        this.awaitingInput = inputType;
    }

    public boolean isAwaitingInput(AwaitingInput inputType) {
        return this.awaitingInput == inputType;
    }

    public InteractionContext currentContext() {
        return this.context;
    }

    public void clearAwaitingInput() {
        this.awaitingInput = null;
        this.context = null;
    }

    // ========================================================================
    // Sub-state accessors
    // ========================================================================

    public CardChoiceState cardChoice() {
        return cardChoice;
    }

    public PermanentChoiceState permanentChoice() {
        return permanentChoice;
    }

    public GraveyardChoiceState graveyardChoice() {
        return graveyardChoice;
    }

    public ColorChoiceState colorChoice() {
        return colorChoice;
    }

    public LibrarySearchState librarySearch() {
        return librarySearch;
    }

    public LibraryViewState libraryView() {
        return libraryView;
    }

    public RevealedHandChoiceState revealedHandChoice() {
        return revealedHandChoice;
    }

    public MultiSelectionState multiSelection() {
        return multiSelection;
    }

    // ========================================================================
    // Combat
    // ========================================================================

    public void beginAttackerDeclaration(UUID activePlayerId) {
        this.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        this.context = new InteractionContext.AttackerDeclaration(activePlayerId);
    }

    public void beginBlockerDeclaration(UUID defenderId) {
        this.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        this.context = new InteractionContext.BlockerDeclaration(defenderId);
    }

    public void beginCombatDamageAssignment(UUID playerId, int attackerIndex, UUID attackerPermanentId,
                                             String attackerName, int totalDamage,
                                             List<CombatDamageTarget> validTargets, boolean isTrample,
                                             boolean isDeathtouch) {
        this.awaitingInput = AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT;
        this.context = new InteractionContext.CombatDamageAssignment(playerId, attackerIndex, attackerPermanentId,
                attackerName, totalDamage, validTargets, isTrample, isDeathtouch);
    }

    public void clearCombatDamageAssignment() {
        // No extra fields to clear — context is cleared by clearAwaitingInput()
    }

    public InteractionContext.CombatDamageAssignment combatDamageAssignmentContext() {
        if (context instanceof InteractionContext.CombatDamageAssignment cda) return cda;
        return null;
    }

    // ========================================================================
    // Card choice
    // ========================================================================

    public void beginCardChoice(AwaitingInput type, UUID playerId, Set<Integer> validIndices, UUID targetPermanentId) {
        this.awaitingInput = type;
        this.cardChoice = new CardChoiceState(playerId, new HashSet<>(validIndices), targetPermanentId);
        this.context = new InteractionContext.CardChoice(type, playerId, new HashSet<>(validIndices), targetPermanentId);
    }

    public void clearCardChoice() {
        this.cardChoice = null;
    }

    public InteractionContext.CardChoice cardChoiceContext() {
        if (context instanceof InteractionContext.CardChoice cc) return cc;
        if (cardChoice == null || awaitingInput == null) return null;
        return new InteractionContext.CardChoice(awaitingInput, cardChoice.playerId(),
                cardChoice.validIndices(), cardChoice.targetPermanentId());
    }

    // ========================================================================
    // Permanent choice
    // ========================================================================

    public void beginPermanentChoice(UUID playerId, Set<UUID> validIds, PermanentChoiceContext choiceContext) {
        this.awaitingInput = AwaitingInput.PERMANENT_CHOICE;
        this.permanentChoice = new PermanentChoiceState(playerId, new HashSet<>(validIds));
        this.permanentChoiceContext = choiceContext;
        this.context = new InteractionContext.PermanentChoice(playerId, new HashSet<>(validIds), choiceContext);
    }

    public void clearPermanentChoice() {
        this.permanentChoice = null;
        this.permanentChoiceContext = null;
    }

    public void setPermanentChoiceContext(PermanentChoiceContext choiceContext) {
        this.permanentChoiceContext = choiceContext;
    }

    public PermanentChoiceContext permanentChoiceContext() {
        return this.permanentChoiceContext;
    }

    public void clearPermanentChoiceContext() {
        this.permanentChoiceContext = null;
    }

    public InteractionContext.PermanentChoice permanentChoiceContextView() {
        if (context instanceof InteractionContext.PermanentChoice pc) return pc;
        if (permanentChoice == null) return null;
        return new InteractionContext.PermanentChoice(permanentChoice.playerId(),
                permanentChoice.validIds(), permanentChoiceContext);
    }

    // ========================================================================
    // Pending aura
    // ========================================================================

    public void setPendingAuraCard(Card auraCard) {
        this.pendingAuraCard = auraCard;
    }

    public Card pendingAuraCard() {
        return this.pendingAuraCard;
    }

    public Card consumePendingAuraCard() {
        Card auraCard = this.pendingAuraCard;
        this.pendingAuraCard = null;
        return auraCard;
    }

    public void setPendingAuraOwnerId(UUID ownerId) {
        this.pendingAuraOwnerId = ownerId;
    }

    public UUID consumePendingAuraOwnerId() {
        UUID ownerId = this.pendingAuraOwnerId;
        this.pendingAuraOwnerId = null;
        return ownerId;
    }

    // ========================================================================
    // Graveyard choice
    // ========================================================================

    public void beginGraveyardChoice(UUID playerId, Set<Integer> validIndices,
                                     GraveyardChoiceDestination destination, List<Card> cardPool) {
        this.awaitingInput = AwaitingInput.GRAVEYARD_CHOICE;
        if (this.graveyardChoice == null) {
            this.graveyardChoice = new GraveyardChoiceState(playerId, new HashSet<>(validIndices), destination, cardPool);
        } else {
            // Preserve independently-set fields (gainLife, attachToSource, grantColor, grantSubtype)
            GraveyardChoiceState prev = this.graveyardChoice;
            this.graveyardChoice = new GraveyardChoiceState(playerId, new HashSet<>(validIndices), destination, cardPool);
            this.graveyardChoice.setGainLifeEqualToManaValue(prev.gainLifeEqualToManaValue());
            this.graveyardChoice.setAttachToSourcePermanentId(prev.attachToSourcePermanentId());
            this.graveyardChoice.setGrantColor(prev.grantColor());
            this.graveyardChoice.setGrantSubtype(prev.grantSubtype());
        }
        this.context = new InteractionContext.GraveyardChoice(playerId, new HashSet<>(validIndices), destination, cardPool);
    }

    public void clearGraveyardChoice() {
        this.graveyardChoice = null;
    }

    public void prepareGraveyardChoice(GraveyardChoiceDestination destination, List<Card> cardPool) {
        ensureGraveyardChoice().setDestination(destination);
        ensureGraveyardChoice().setCardPool(cardPool);
    }

    public void setGraveyardChoiceGainLifeEqualToManaValue(boolean value) {
        ensureGraveyardChoice().setGainLifeEqualToManaValue(value);
    }

    public void setGraveyardChoiceAttachToSourcePermanentId(UUID permanentId) {
        ensureGraveyardChoice().setAttachToSourcePermanentId(permanentId);
    }

    public void setGraveyardChoiceGrantColor(CardColor color) {
        ensureGraveyardChoice().setGrantColor(color);
    }

    public void setGraveyardChoiceGrantSubtype(CardSubtype subtype) {
        ensureGraveyardChoice().setGrantSubtype(subtype);
    }

    public InteractionContext.GraveyardChoice graveyardChoiceContext() {
        if (context instanceof InteractionContext.GraveyardChoice gc) return gc;
        if (graveyardChoice == null || graveyardChoice.playerId() == null) return null;
        return new InteractionContext.GraveyardChoice(graveyardChoice.playerId(),
                graveyardChoice.validIndices(), graveyardChoice.destination(), graveyardChoice.cardPool());
    }

    private GraveyardChoiceState ensureGraveyardChoice() {
        if (this.graveyardChoice == null) {
            this.graveyardChoice = new GraveyardChoiceState();
        }
        return this.graveyardChoice;
    }

    // ========================================================================
    // Equipment attach
    // ========================================================================

    public void setPendingEquipmentAttach(UUID equipmentPermanentId, UUID targetPermanentId) {
        this.pendingEquipmentAttachEquipmentId = equipmentPermanentId;
        this.pendingEquipmentAttachTargetId = targetPermanentId;
    }

    public UUID pendingEquipmentAttachEquipmentId() {
        return this.pendingEquipmentAttachEquipmentId;
    }

    public UUID pendingEquipmentAttachTargetId() {
        return this.pendingEquipmentAttachTargetId;
    }

    public void clearPendingEquipmentAttach() {
        this.pendingEquipmentAttachEquipmentId = null;
        this.pendingEquipmentAttachTargetId = null;
    }

    // ========================================================================
    // Color choice
    // ========================================================================

    public void beginColorChoice(UUID playerId, UUID permanentId, UUID etbTargetPermanentId,
                                 ColorChoiceContext choiceContext) {
        this.awaitingInput = AwaitingInput.COLOR_CHOICE;
        this.colorChoice = new ColorChoiceState(playerId, permanentId, etbTargetPermanentId, choiceContext);
        this.context = new InteractionContext.ColorChoice(playerId, permanentId, etbTargetPermanentId, choiceContext);
    }

    public void clearColorChoice() {
        this.colorChoice = null;
    }

    public ColorChoiceContext colorChoiceContext() {
        return colorChoice != null ? colorChoice.choiceContext() : null;
    }

    public InteractionContext.ColorChoice colorChoiceContextView() {
        if (context instanceof InteractionContext.ColorChoice cc) return cc;
        if (colorChoice == null) return null;
        return new InteractionContext.ColorChoice(colorChoice.playerId(), colorChoice.permanentId(),
                colorChoice.etbTargetPermanentId(), colorChoice.choiceContext());
    }

    // ========================================================================
    // May ability choice
    // ========================================================================

    public void beginMayAbilityChoice(UUID playerId, String description) {
        this.awaitingInput = AwaitingInput.MAY_ABILITY_CHOICE;
        this.awaitingMayAbilityPlayerId = playerId;
        this.context = new InteractionContext.MayAbilityChoice(playerId, description);
    }

    public void clearMayAbilityChoice() {
        this.awaitingMayAbilityPlayerId = null;
    }

    public UUID awaitingMayAbilityPlayerId() {
        return this.awaitingMayAbilityPlayerId;
    }

    public InteractionContext.MayAbilityChoice mayAbilityChoiceContext() {
        if (context instanceof InteractionContext.MayAbilityChoice mc) return mc;
        if (awaitingMayAbilityPlayerId == null) return null;
        return new InteractionContext.MayAbilityChoice(awaitingMayAbilityPlayerId, "");
    }

    // ========================================================================
    // Multi-permanent choice
    // ========================================================================

    public void beginMultiPermanentChoice(UUID playerId, Set<UUID> validIds, int maxCount) {
        this.awaitingInput = AwaitingInput.MULTI_PERMANENT_CHOICE;
        this.multiSelection.setMultiPermanent(playerId, new HashSet<>(validIds), maxCount);
        this.context = new InteractionContext.MultiPermanentChoice(playerId, new HashSet<>(validIds), maxCount);
    }

    public void clearMultiPermanentChoice() {
        this.multiSelection.clearMultiPermanent();
    }

    public InteractionContext.MultiPermanentChoice multiPermanentChoiceContext() {
        if (context instanceof InteractionContext.MultiPermanentChoice mpc) return mpc;
        if (multiSelection.multiPermanentPlayerId() == null || multiSelection.multiPermanentValidIds() == null) return null;
        return new InteractionContext.MultiPermanentChoice(multiSelection.multiPermanentPlayerId(),
                multiSelection.multiPermanentValidIds(), multiSelection.multiPermanentMaxCount());
    }

    // ========================================================================
    // Multi-graveyard choice
    // ========================================================================

    public void beginMultiGraveyardChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) {
        this.awaitingInput = AwaitingInput.MULTI_GRAVEYARD_CHOICE;
        this.multiSelection.setMultiGraveyard(playerId, new HashSet<>(validCardIds), maxCount);
        this.context = new InteractionContext.MultiGraveyardChoice(playerId, new HashSet<>(validCardIds), maxCount);
    }

    public void clearMultiGraveyardChoice() {
        this.multiSelection.clearMultiGraveyard();
    }

    public InteractionContext.MultiGraveyardChoice multiGraveyardChoiceContext() {
        if (context instanceof InteractionContext.MultiGraveyardChoice mgc) return mgc;
        if (multiSelection.multiGraveyardPlayerId() == null || multiSelection.multiGraveyardValidCardIds() == null) return null;
        return new InteractionContext.MultiGraveyardChoice(multiSelection.multiGraveyardPlayerId(),
                multiSelection.multiGraveyardValidCardIds(), multiSelection.multiGraveyardMaxCount());
    }

    // ========================================================================
    // Library reorder
    // ========================================================================

    public void beginLibraryReorder(UUID playerId, List<Card> cards, boolean toBottom) {
        beginLibraryReorder(playerId, cards, toBottom, playerId);
    }

    public void beginLibraryReorder(UUID playerId, List<Card> cards, boolean toBottom, UUID deckOwnerId) {
        this.awaitingInput = AwaitingInput.LIBRARY_REORDER;
        this.libraryView.setReorder(playerId, cards, toBottom);
        this.context = new InteractionContext.LibraryReorder(playerId, cards, toBottom, deckOwnerId);
    }

    public void clearLibraryReorder() {
        this.libraryView.clearReorder();
    }

    public InteractionContext.LibraryReorder libraryReorderContext() {
        if (context instanceof InteractionContext.LibraryReorder lr) return lr;
        if (libraryView.reorderPlayerId() == null || libraryView.reorderCards() == null) return null;
        return new InteractionContext.LibraryReorder(libraryView.reorderPlayerId(),
                libraryView.reorderCards(), libraryView.reorderToBottom());
    }

    // ========================================================================
    // Scry
    // ========================================================================

    public void beginScry(UUID playerId, List<Card> cards) {
        this.awaitingInput = AwaitingInput.SCRY;
        this.libraryView.setScry(playerId, cards);
        this.context = new InteractionContext.Scry(playerId, cards);
    }

    public void clearScry() {
        this.libraryView.clearScry();
    }

    public InteractionContext.Scry scryContext() {
        if (context instanceof InteractionContext.Scry s) return s;
        if (libraryView.scryPlayerId() == null || libraryView.scryCards() == null) return null;
        return new InteractionContext.Scry(libraryView.scryPlayerId(), libraryView.scryCards());
    }

    // ========================================================================
    // Library search
    // ========================================================================

    public void beginLibrarySearch(LibrarySearchParams params) {
        this.awaitingInput = AwaitingInput.LIBRARY_SEARCH;
        this.librarySearch = new LibrarySearchState(
                params.playerId(), params.cards(), params.reveals(), params.canFailToFind(),
                params.targetPlayerId(), params.remainingCount(), params.sourceCards(),
                params.reorderRemainingToBottom(), params.reorderRemainingToTop(),
                params.shuffleAfterSelection(),
                params.prompt(), params.destination(), params.filterCardTypes(),
                params.accumulatedCards(), params.filterCardName()
        );
        this.context = new InteractionContext.LibrarySearch(params.playerId(), params.cards(), params.reveals(),
                params.canFailToFind(), params.targetPlayerId(), params.remainingCount(), params.sourceCards(),
                params.reorderRemainingToBottom(), params.reorderRemainingToTop(),
                params.shuffleAfterSelection(), params.prompt(), params.destination(),
                params.filterCardTypes(), params.accumulatedCards(), params.filterCardName());
    }

    public void clearLibrarySearch() {
        this.librarySearch = null;
    }

    public InteractionContext.LibrarySearch librarySearchContext() {
        if (context instanceof InteractionContext.LibrarySearch ls) return ls;
        if (librarySearch == null) return null;
        return new InteractionContext.LibrarySearch(librarySearch.playerId(), librarySearch.cards(),
                librarySearch.reveals(), librarySearch.canFailToFind(),
                librarySearch.targetPlayerId(), librarySearch.remainingCount(),
                librarySearch.sourceCards(), librarySearch.reorderRemainingToBottom(),
                librarySearch.reorderRemainingToTop(),
                librarySearch.shuffleAfterSelection(), librarySearch.prompt(), librarySearch.destination(),
                librarySearch.filterCardTypes(), librarySearch.accumulatedCards(),
                librarySearch.filterCardName());
    }

    // ========================================================================
    // Library reveal choice
    // ========================================================================

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds) {
        beginLibraryRevealChoice(playerId, allCards, validCardIds, false);
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds,
                                         boolean remainingToGraveyard) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.libraryView.setReveal(playerId, allCards, validCardIds);
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds, remainingToGraveyard);
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds,
                                          boolean remainingToGraveyard, boolean selectedToHand,
                                          boolean reorderRemainingToBottom) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.libraryView.setReveal(playerId, allCards, validCardIds);
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds,
                remainingToGraveyard, selectedToHand, reorderRemainingToBottom);
    }

    public void clearLibraryRevealChoice() {
        this.libraryView.clearReveal();
    }

    public InteractionContext.LibraryRevealChoice libraryRevealChoiceContext() {
        if (context instanceof InteractionContext.LibraryRevealChoice lrc) return lrc;
        if (libraryView.revealPlayerId() == null || libraryView.revealAllCards() == null
                || libraryView.revealValidCardIds() == null) return null;
        return new InteractionContext.LibraryRevealChoice(libraryView.revealPlayerId(),
                libraryView.revealAllCards(), libraryView.revealValidCardIds(), false);
    }

    // ========================================================================
    // Hand top/bottom choice
    // ========================================================================

    public void beginHandTopBottomChoice(UUID playerId, List<Card> cards) {
        this.awaitingInput = AwaitingInput.HAND_TOP_BOTTOM_CHOICE;
        this.libraryView.setHandTopBottom(playerId, cards);
        this.context = new InteractionContext.HandTopBottomChoice(playerId, cards);
    }

    public void clearHandTopBottomChoice() {
        this.libraryView.clearHandTopBottom();
    }

    public InteractionContext.HandTopBottomChoice handTopBottomChoiceContext() {
        if (context instanceof InteractionContext.HandTopBottomChoice htbc) return htbc;
        if (libraryView.handTopBottomPlayerId() == null || libraryView.handTopBottomCards() == null) return null;
        return new InteractionContext.HandTopBottomChoice(libraryView.handTopBottomPlayerId(),
                libraryView.handTopBottomCards());
    }

    // ========================================================================
    // Revealed hand choice
    // ========================================================================

    public void beginRevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId, Set<Integer> validIndices,
                                        int remainingCount, boolean discardMode, List<Card> chosenCards) {
        this.awaitingInput = AwaitingInput.REVEALED_HAND_CHOICE;
        this.revealedHandChoice = new RevealedHandChoiceState(
                choosingPlayerId, new HashSet<>(validIndices), targetPlayerId,
                remainingCount, discardMode, chosenCards
        );
        // Also update cardChoice for backwards compatibility (shared fields)
        this.cardChoice = new CardChoiceState(choosingPlayerId, new HashSet<>(validIndices), null);
        this.context = new InteractionContext.RevealedHandChoice(
                choosingPlayerId, targetPlayerId, new HashSet<>(validIndices),
                remainingCount, discardMode,
                new ArrayList<>(revealedHandChoice.chosenCardsSnapshot())
        );
    }

    public void beginRevealedHandChoiceFromCurrentState(UUID choosingPlayerId, UUID targetPlayerId,
                                                        Set<Integer> validIndices) {
        if (revealedHandChoice == null) return;
        List<Card> chosenCardsSnapshot = revealedHandChoice.chosenCardsSnapshot();
        beginRevealedHandChoice(
                choosingPlayerId, targetPlayerId, validIndices,
                revealedHandChoice.remainingCount(),
                revealedHandChoice.discardMode(),
                chosenCardsSnapshot
        );
    }

    public void addRevealedHandChosenCard(Card card) {
        if (revealedHandChoice != null) {
            revealedHandChoice.addChosenCard(card);
        }
    }

    public int decrementRevealedHandChoiceRemainingCount() {
        return revealedHandChoice != null ? revealedHandChoice.decrementRemainingCount() : 0;
    }

    public void clearRevealedHandChoiceProgress() {
        if (revealedHandChoice != null) {
            revealedHandChoice.clearProgress();
        }
    }

    public InteractionContext.RevealedHandChoice revealedHandChoiceContext() {
        if (context instanceof InteractionContext.RevealedHandChoice rhc) return rhc;
        if (revealedHandChoice == null || cardChoice == null) return null;
        return new InteractionContext.RevealedHandChoice(
                revealedHandChoice.choosingPlayerId(), revealedHandChoice.targetPlayerId(),
                revealedHandChoice.validIndices(), revealedHandChoice.remainingCount(),
                revealedHandChoice.discardMode(), revealedHandChoice.chosenCardsSnapshot());
    }

    // ========================================================================
    // Discard
    // ========================================================================

    public void setDiscardRemainingCount(int remainingCount) {
        ensureRevealedHandChoice().setDiscardRemainingCount(remainingCount);
    }

    public int decrementDiscardRemainingCount() {
        return revealedHandChoice != null ? revealedHandChoice.decrementDiscardRemainingCount() : 0;
    }

    private RevealedHandChoiceState ensureRevealedHandChoice() {
        if (this.revealedHandChoice == null) {
            this.revealedHandChoice = new RevealedHandChoiceState(null, null, null, 0, false, null);
        }
        return this.revealedHandChoice;
    }

    // ========================================================================
    // Multi-zone exile choice
    // ========================================================================

    public void beginMultiZoneExileChoice(UUID playerId, Set<UUID> validCardIds, int maxCount,
                                          UUID targetPlayerId, UUID controllerId, String cardName) {
        this.awaitingInput = AwaitingInput.MULTI_ZONE_EXILE_CHOICE;
        this.multiSelection.setMultiZoneExile(playerId, new HashSet<>(validCardIds), maxCount);
        this.context = new InteractionContext.MultiZoneExileChoice(playerId, new HashSet<>(validCardIds),
                maxCount, targetPlayerId, controllerId, cardName);
    }

    public void clearMultiZoneExileChoice() {
        this.multiSelection.clearMultiZoneExile();
    }

    public InteractionContext.MultiZoneExileChoice multiZoneExileChoiceContext() {
        if (context instanceof InteractionContext.MultiZoneExileChoice mzec) return mzec;
        if (multiSelection.multiZoneExilePlayerId() == null || multiSelection.multiZoneExileValidCardIds() == null) return null;
        return new InteractionContext.MultiZoneExileChoice(multiSelection.multiZoneExilePlayerId(),
                multiSelection.multiZoneExileValidCardIds(), multiSelection.multiZoneExileMaxCount(),
                null, null, null);
    }

    // ========================================================================
    // X value choice
    // ========================================================================

    public void beginXValueChoice(UUID playerId, int maxValue, String prompt, String cardName) {
        this.awaitingInput = AwaitingInput.X_VALUE_CHOICE;
        this.context = new InteractionContext.XValueChoice(playerId, maxValue, prompt, cardName);
    }

    public InteractionContext.XValueChoice xValueChoiceContext() {
        if (context instanceof InteractionContext.XValueChoice xvc) return xvc;
        return null;
    }

    // ========================================================================
    // Knowledge pool cast choice
    // ========================================================================

    public void beginKnowledgePoolCastChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) {
        this.awaitingInput = AwaitingInput.KNOWLEDGE_POOL_CAST_CHOICE;
        this.multiSelection.setMultiGraveyard(playerId, new HashSet<>(validCardIds), maxCount);
        this.context = new InteractionContext.KnowledgePoolCastChoice(playerId, new HashSet<>(validCardIds), maxCount);
    }

    public void clearKnowledgePoolCastChoice() {
        this.multiSelection.clearMultiGraveyard();
    }

    public InteractionContext.KnowledgePoolCastChoice knowledgePoolCastChoiceContext() {
        if (context instanceof InteractionContext.KnowledgePoolCastChoice kpc) return kpc;
        if (multiSelection.multiGraveyardPlayerId() == null || multiSelection.multiGraveyardValidCardIds() == null) return null;
        return new InteractionContext.KnowledgePoolCastChoice(multiSelection.multiGraveyardPlayerId(),
                multiSelection.multiGraveyardValidCardIds(), multiSelection.multiGraveyardMaxCount());
    }
}
