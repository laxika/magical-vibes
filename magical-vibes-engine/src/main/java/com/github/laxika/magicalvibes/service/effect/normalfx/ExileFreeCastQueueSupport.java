package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingFreeCastLifeLoss;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayLifeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnControlledCreatureOrPayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.cost.AdditionalSpellCostService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Casts a queue of exiled cards "without paying their mana costs" one at a time, so a cast that
 * pauses for target selection can resume the remaining ones (Improvisation Capstone, Hazoret's
 * Undying Fury, Brilliant Ultimatum, Spelltwine's two copies).
 *
 * <p>Entries queued through {@link #queueCopiesForFreeCast} are copies rather than real cards: they
 * are cast as copies and cease to exist instead of being put into a zone.</p>
 */
@Slf4j
@Component
public class ExileFreeCastQueueSupport {

    private static final String COUNTERS_OPTION = "Put counters on a creature you control";

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final InputCompletionService inputCompletionService;
    private final GraveyardService graveyardService;
    private final CopySupport copySupport;
    private final SpellweaverVoluteSupport spellweaverVoluteSupport;
    private final AdditionalSpellCostService additionalSpellCostService;
    private final SpellCastingService spellCastingService;
    private final CastingPermissionService castingPermissionService;
    private final OutsideGameNormalCostCastSupport outsideGameNormalCostCastSupport;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private BrilliantUltimatumSupport brilliantUltimatumSupport;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.battlefield.GameQueryService gameQueryService;
    @org.springframework.beans.factory.annotation.Autowired
    private com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService predicateEvaluationService;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private LifeSupport lifeSupport;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.battlefield.DepartureService departureService;

    // @Lazy mirrors ExileFreeCastSupport: breaks the cycle back through the input services.
    public ExileFreeCastQueueSupport(GameLogService gameLogService,
                                            PlayerInputService playerInputService,
                                            TriggerCollectionService triggerCollectionService,
                                            ExileCastTargetSupport exileCastTargetSupport,
                                            @Lazy InputCompletionService inputCompletionService,
                                            GraveyardService graveyardService,
                                            CopySupport copySupport,
                                            @Lazy SpellweaverVoluteSupport spellweaverVoluteSupport,
                                            AdditionalSpellCostService additionalSpellCostService,
                                            @Lazy SpellCastingService spellCastingService,
                                            CastingPermissionService castingPermissionService,
                                            @Lazy OutsideGameNormalCostCastSupport outsideGameNormalCostCastSupport) {
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.inputCompletionService = inputCompletionService;
        this.graveyardService = graveyardService;
        this.copySupport = copySupport;
        this.spellweaverVoluteSupport = spellweaverVoluteSupport;
        this.additionalSpellCostService = additionalSpellCostService;
        this.spellCastingService = spellCastingService;
        this.castingPermissionService = castingPermissionService;
        this.outsideGameNormalCostCastSupport = outsideGameNormalCostCastSupport;
    }

    public void castChosenSpellsWithoutPaying(GameData gameData, Player player, List<UUID> cardIds) {
        gameData.interaction.clearAwaitingInput();

        if (cardIds == null || cardIds.isEmpty()) {
            String logEntry = player.getUsername() + " casts no spells for free.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            finishFreeCastProcess(gameData);
            return;
        }

        gameData.pendingFreeCastQueue.clear();
        gameData.pendingFreeCastQueue.addAll(cardIds);
        castNextFromQueue(gameData, player.getId());
    }

    /** Casts the selected Eye of the Storm copies in the order chosen by their caster. */
    public void castChosenCopiesWithoutPaying(GameData gameData, UUID playerId, List<UUID> copyIds) {
        gameData.interaction.clearAwaitingInput();
        queueCopiesForFreeCast(gameData, playerId, copyIds);
    }

    /**
     * Queues copies sitting in exile to be cast for free, in the given order, and starts the chain.
     * Each is cast as a copy, so it ceases to exist on resolution (CR 707.10a).
     */
    public void queueCopiesForFreeCast(GameData gameData, UUID playerId, List<UUID> copyIds) {
        queueCopiesForFreeCast(gameData, playerId, copyIds, 0);
    }

    /** As above, for copies whose X was announced and paid for when the cast was offered (CR 107.3). */
    public void queueCopiesForFreeCast(GameData gameData, UUID playerId, List<UUID> copyIds, int xValue) {
        if (xValue > 0) {
            copyIds.forEach(id -> gameData.pendingFreeCastXValues.put(id, xValue));
        }
        gameData.pendingFreeCastQueue.addAll(copyIds);
        gameData.pendingFreeCastAsCopyIds.addAll(copyIds);
        castNextFromQueue(gameData, playerId);
    }

    /**
     * Moves every still-exiled card tracked in
     * {@link GameData#pendingExileFreeCastRemainderToGraveyard} to its owner's graveyard, then
     * clears the list. No-op when the list is empty (Improvisation Capstone / Hazoret's Undying Fury).
     */
    public void putRemainderIntoOwnersGraveyards(GameData gameData) {
        List<UUID> remainder = new ArrayList<>(gameData.pendingExileFreeCastRemainderToGraveyard);
        gameData.pendingExileFreeCastRemainderToGraveyard.clear();
        for (UUID cardId : remainder) {
            ExiledCardEntry entry = gameData.findExiledCard(cardId);
            if (entry == null) {
                continue;
            }
            gameData.removeFromExile(cardId);
            graveyardService.addCardToGraveyard(gameData, entry.ownerId(), entry.card());
            gameLogService.append(gameData, GameLog.cardThen(entry.card(), " is put into its owner's graveyard."));
        }
    }

    /** Moves every still-exiled tracked card into its owner's hand, then clears the list. */
    public void putRemainderIntoOwnersHands(GameData gameData) {
        List<UUID> remainder = new ArrayList<>(gameData.pendingExileFreeCastRemainderToHand);
        gameData.pendingExileFreeCastRemainderToHand.clear();
        for (UUID cardId : remainder) {
            ExiledCardEntry entry = gameData.findExiledCard(cardId);
            if (entry == null || !gameData.removeFromExile(cardId)) {
                continue;
            }
            gameData.playerHands.computeIfAbsent(entry.ownerId(), ignored -> new ArrayList<>()).add(entry.card());
            gameLogService.append(gameData, GameLog.cardThen(entry.card(), " is put into its owner's hand."));
        }
    }

    public void queueRemainderToLibraryBottom(GameData gameData, List<UUID> cardIds) {
        gameData.pendingExileFreeCastRemainderToLibraryBottom.clear();
        gameData.pendingExileFreeCastRemainderToLibraryBottom.addAll(cardIds);
    }

    public void putRemainderIntoLibraryBottom(GameData gameData) {
        List<UUID> remainder = new ArrayList<>(gameData.pendingExileFreeCastRemainderToLibraryBottom);
        gameData.pendingExileFreeCastRemainderToLibraryBottom.clear();
        Map<UUID, List<Card>> cardsByOwner = new HashMap<>();
        for (UUID cardId : remainder) {
            ExiledCardEntry entry = gameData.findExiledCard(cardId);
            if (entry == null || !gameData.removeFromExile(cardId)) {
                continue;
            }
            cardsByOwner.computeIfAbsent(entry.ownerId(), ignored -> new ArrayList<>()).add(entry.card());
        }
        cardsByOwner.forEach((ownerId, cards) -> {
            Collections.shuffle(cards);
            gameData.playerDecks.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).addAll(cards);
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(ownerId)
                    + " puts the uncast cards on the bottom of their library in a random order."));
        });
    }


    /**
     * Casts the next queued exiled spell. When a spell requires a target this pauses for a target
     * choice and returns; the shared target handler resumes the queue via {@link #castNextFromQueue}
     * once the target is chosen.
     *
     * <p>Once the queue drains this ends through the shared {@link InputCompletionService} epilogue,
     * which resumes the stack entry parked in {@code GameData.pendingEffectResolutionEntry} when the
     * cast choice was begun. Ending any other way leaves that entry dangling, which wedges
     * {@code GameData.deferPlayerLossCheck} so no player can ever lose to a state-based action again.
     */
    public void castNextFromQueue(GameData gameData, UUID playerId) {
        if (gameData.pendingFreeCastQueue.isEmpty()) {
            finishFreeCastProcess(gameData);
            return;
        }

        UUID cardId = gameData.pendingFreeCastQueue.removeFirst();
        boolean asCopy = gameData.pendingFreeCastAsCopyIds.remove(cardId);
        int announcedXValue = gameData.pendingFreeCastXValues.getOrDefault(cardId, 0);
        gameData.pendingFreeCastXValues.remove(cardId);
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        if (exiledEntry == null) {
            castNextFromQueue(gameData, playerId);
            return;
        }

        Card card = exiledEntry.card();
        if (card.hasType(CardType.LAND) && gameData.pendingFreeCastLandPlayIds.remove(cardId)) {
            brilliantUltimatumSupport.playLandFromExile(gameData, playerId, card);
            if (!gameData.interaction.isAwaitingInput()) castNextFromQueue(gameData, playerId);
            return;
        }
        // A copy is still cast, so its own "cast only during ..." restriction still applies (Elite Arcanist ruling).
        if (card.isCastOnlyFromGraveyard()
                || !castingPermissionService.isSpellCastingAllowed(gameData, playerId, card)
                || (asCopy && !castingPermissionService.canCastWithSpellTimingRestriction(gameData, playerId, card))) {
            if (asCopy) {
                gameData.removeFromExile(cardId);
            }
            gameLogService.append(gameData, GameLog.cardThen(card,
                    asCopy ? " cannot be cast from exile." : " cannot be cast from exile and stays exiled."));
            castNextFromQueue(gameData, playerId);
            return;
        }
        boolean asAdventure = !asCopy && gameData.pendingFreeCastAdventureIds.remove(cardId)
                && card.getBackFaceCard() != null
                && card.getCastingOption(AdventureCast.class).isPresent();
        Card cardToCast;
        if (asAdventure) {
            cardToCast = card.createRuntimeCopyWithFace(card.getBackFaceCard());
            cardToCast.freeze();
        } else {
            cardToCast = asCopy
                    && !card.hasType(CardType.INSTANT)
                    && !card.hasType(CardType.SORCERY)
                    ? copySupport.createTokenCopyCard(card)
                    : card;
        }
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(cardToCast);
        List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
        ChooseOneEffect modal = spellEffects.stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .orElse(null);
        AdditionalSpellCostService.ExtractedCosts additionalCosts =
                additionalSpellCostService.peek(gameData, playerId, cardToCast);

        if (modal != null) {
            if (additionalCosts.hasNonEscalateCost()) {
                skipCardWithUnsupportedCost(gameData, playerId, card, asCopy);
                return;
            }
            int maximumChoices = additionalCosts.hasEscalate()
                    ? maxEscalateModes(gameData, playerId, cardToCast, additionalCosts, modal.choicesMax())
                    : modal.choicesMax();
            if (modal.choicesRequired() > maximumChoices) {
                skipCardWithUnsupportedCost(gameData, playerId, card, asCopy);
                return;
            }
            Card runtimeCard = cardToCast.createRuntimeCopy();
            List<Integer> offeredModes = legalNextModeIndices(
                    gameData, runtimeCard, playerId, modal, List.of(), maximumChoices);
            if (offeredModes.isEmpty()) {
                skipCardWithoutLegalMode(gameData, playerId, card, asCopy);
                return;
            }
            playerInputService.beginExileFreeCastModeChoice(
                    gameData, playerId, runtimeCard, modal, spellType,
                    List.of(), offeredModes, maximumChoices, asCopy);
            return;
        }

        spendManaValueBudget(gameData, cardToCast.getManaValue());
        if (additionalCosts.any()) {
            if (spellEffects.stream().filter(CostEffect.class::isInstance).count() == 1
                    && (additionalCosts.discardCardOrPayManaCost() != null || additionalCosts.discardCost() != null)
                    && additionalSpellCostService.satisfiableWithoutManaCost(gameData, playerId, cardToCast)) {
                castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
                return;
            }
            if (spellEffects.stream().filter(CostEffect.class::isInstance).count() == 1
                    && additionalCosts.discardCardOrPayLifeCost() != null
                    && additionalSpellCostService.satisfiableWithoutManaCost(gameData, playerId, cardToCast)) {
                castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
                return;
            }
            if (spellEffects.stream().filter(CostEffect.class::isInstance).count() == 1
                    && additionalCosts.tapMultipleCost() != null
                    && additionalSpellCostService.satisfiable(gameData, playerId, cardToCast)) {
                castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
                return;
            }
            if (spellEffects.stream().filter(CostEffect.class::isInstance).count() == 1
                    && additionalCosts.sacrificeAllCreatures()
                    && additionalSpellCostService.satisfiable(gameData, playerId, cardToCast)) {
                castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
                return;
            }
            if (spellEffects.stream().filter(CostEffect.class::isInstance).count() == 1
                    && additionalCosts.sacrificePermanentOrDiscardCardCost() != null
                    && additionalSpellCostService.satisfiableWithoutManaCost(gameData, playerId, cardToCast)) {
                castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
                return;
            }
            if (spellEffects.stream().filter(CostEffect.class::isInstance).count() == 1
                    && additionalCosts.sacrificePermanentCost() != null
                    && !additionalCosts.sacrificePermanentCost().optional()
                    && additionalSpellCostService.satisfiable(gameData, playerId, cardToCast)) {
                castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
                return;
            }
            if (additionalCosts.sacrificeCreature()
                    && spellEffects.stream().filter(CostEffect.class::isInstance)
                    .allMatch(com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost.class::isInstance)
                    && additionalSpellCostService.satisfiable(gameData, playerId, cardToCast)) {
                castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
                return;
            }
            if (modal == null && additionalCosts.discardCost() != null
                    && spellEffects.stream().filter(CostEffect.class::isInstance)
                    .allMatch(DiscardCardTypeCost.class::isInstance)
                    && !needsCastTarget(cardToCast, spellEffects)
                    && additionalSpellCostService.satisfiable(gameData, playerId, cardToCast)) {
                List<Integer> validIndices = additionalSpellCostService.validDiscardCostIndices(
                        gameData, playerId, cardToCast);
                spellEffects.removeIf(DiscardCardTypeCost.class::isInstance);
                StackEntry pendingCast = new StackEntry(spellType, cardToCast, playerId,
                        cardToCast.getName(), spellEffects, 0, (UUID) null, null);
                pendingCast.setCopy(asCopy);
                pendingCast.setCastCopy(asCopy);
                if (!asCopy) {
                    pendingCast.setSourceZone(Zone.EXILE);
                    pendingCast.setOwnerIdOverride(exiledEntry.ownerId());
                }
                playerInputService.beginDiscardChoice(gameData, playerId, validIndices,
                        "Choose a card to discard to cast " + cardToCast.getName() + ".",
                        additionalCosts.discardCost().count(),
                        DiscardFollowUp.NONE.withPendingSpellCast(pendingCast));
                return;
            }
            if (additionalCosts.discardCost() != null
                    && spellEffects.stream().filter(CostEffect.class::isInstance)
                    .allMatch(DiscardCardTypeCost.class::isInstance)
                    && needsCastTarget(cardToCast, spellEffects)
                    && additionalSpellCostService.satisfiable(gameData, playerId, cardToCast)) {
                castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
                return;
            }
            skipCardWithUnsupportedCost(gameData, playerId, card, asCopy);
            return;
        }

        castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, announcedXValue);
    }

    public void handleModeChoice(GameData gameData, Player player, String chosenLabel,
                                 ChoiceContext.ExileFreeCastModeChoice context) {
        if (!player.getId().equals(context.controllerId())) {
            throw new IllegalStateException("Not your mode choice");
        }

        List<Integer> chosenModeIndices = new ArrayList<>(context.chosenModeIndices());
        if (ChooseOneEffect.FINISH_MODE_SELECTION.equals(chosenLabel)) {
            if (chosenModeIndices.size() < context.effect().choicesRequired()) {
                throw new IllegalArgumentException("Not enough modes chosen");
            }
            gameData.interaction.clearAwaitingInput();
            finishModalCast(gameData, player, context, chosenModeIndices);
            return;
        }

        Integer chosenModeIndex = null;
        for (Integer offeredModeIndex : context.offeredModeIndices()) {
            if (context.effect().options().get(offeredModeIndex).label().equals(chosenLabel)) {
                chosenModeIndex = offeredModeIndex;
                break;
            }
        }
        if (chosenModeIndex == null) {
            throw new IllegalArgumentException("Invalid mode: " + chosenLabel);
        }
        chosenModeIndices.add(chosenModeIndex);
        gameData.interaction.clearAwaitingInput();

        if (chosenModeIndices.size() >= context.maximumChoices()) {
            finishModalCast(gameData, player, context, chosenModeIndices);
            return;
        }

        List<Integer> offeredModes = legalNextModeIndices(
                gameData, context.cardToCast(), context.controllerId(), context.effect(),
                chosenModeIndices, context.maximumChoices());
        if (offeredModes.isEmpty()) {
            if (chosenModeIndices.size() < context.effect().choicesRequired()) {
                skipCardWithoutLegalMode(gameData, context.controllerId(), context.cardToCast(), context.copy());
            } else {
                finishModalCast(gameData, player, context, chosenModeIndices);
            }
            return;
        }

        playerInputService.beginExileFreeCastModeChoice(
                gameData, context.controllerId(), context.cardToCast(), context.effect(), context.spellType(),
                chosenModeIndices, offeredModes, context.maximumChoices(), context.copy(), context.payManaCost());
    }

    private void finishModalCast(GameData gameData, Player player,
                                 ChoiceContext.ExileFreeCastModeChoice context,
                                 List<Integer> chosenModeIndices) {
        if (context.payManaCost()) {
            outsideGameNormalCostCastSupport.castPreparedModalCard(
                    gameData, player, context, chosenModeIndices);
            return;
        }
        PreparedModalCast prepared = prepareModalCast(
                gameData, context.cardToCast(), context.controllerId(), context.effect(), chosenModeIndices);
        Card castCard = prepared.card();
        List<CardEffect> castEffects = prepared.effects();
        StackEntryType castSpellType = context.spellType();
        int castXValue = prepared.effectiveXValue();
        // A modal double-faced card whose back-face option was chosen is cast as that face (encoded as 1,
        // as in the library-search cast path), so the back face's spell and permanent are the ones created.
        Card backFace = context.cardToCast().getBackFaceCard();
        if (context.cardToCast().isModalDoubleFaced() && backFace != null && castXValue == 1) {
            castCard = backFace;
            castEffects = new ArrayList<>(backFace.getEffects(EffectSlot.SPELL));
            castSpellType = exileCastTargetSupport.mapCardTypeToSpellType(backFace);
            castXValue = 0;
        }
        spendManaValueBudget(gameData, castCard.getManaValue());
        if (chosenModeIndices.size() > 1
                && additionalSpellCostService.peek(gameData, context.controllerId(), castCard).hasEscalate()) {
            gameData.freeCastEscalateModeCounts.put(castCard.getId(), chosenModeIndices.size());
        }
        List<String> labels = chosenModeIndices.stream()
                .map(context.effect().options()::get)
                .map(ChooseOneEffect.ChooseOneOption::label)
                .toList();
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses " + labels + " for ", castCard, "."));
        castPreparedSpell(gameData, context.controllerId(), context.cardToCast(), castCard,
                castEffects, castSpellType, context.copy(), castXValue);
    }

    private List<Integer> legalNextModeIndices(GameData gameData, Card card, UUID controllerId,
                                               ChooseOneEffect modal, List<Integer> chosenModeIndices,
                                               int maximumChoices) {
        List<Integer> legal = new ArrayList<>();
        for (int modeIndex = 0; modeIndex < modal.options().size(); modeIndex++) {
            if (!modal.modesMayRepeat() && chosenModeIndices.contains(modeIndex)) {
                continue;
            }
            // Queued spells are cast from exile, never from a hand, so a split card can't be fused.
            if (SplitCardCastSupport.isFusedMode(card, modeIndex)) {
                continue;
            }
            List<Integer> trial = new ArrayList<>(chosenModeIndices);
            trial.add(modeIndex);
            if (trial.size() < modal.choicesRequired()
                    || isLegalModalSelection(gameData, card, controllerId, modal, trial, maximumChoices)) {
                legal.add(modeIndex);
            }
        }
        return legal;
    }

    private boolean isLegalModalSelection(GameData gameData, Card card, UUID controllerId,
                                          ChooseOneEffect modal, List<Integer> chosenModeIndices,
                                          int maximumChoices) {
        try {
            PreparedModalCast prepared = prepareModalCast(
                    gameData, card, controllerId, modal, chosenModeIndices, null, maximumChoices);
            if (prepared.card().getManaValue() > availableManaValue(gameData)) {
                return false;
            }
            if (!needsCastTarget(prepared.card(), prepared.effects())) {
                return true;
            }
            if (prepared.card().getMaxTargets() > 1) {
                return exileCastTargetSupport.hasLegalTargetSet(gameData, prepared.card(), controllerId);
            }
            return !exileCastTargetSupport.firstSlotCandidates(
                    gameData, prepared.card(), prepared.effects(), controllerId).isEmpty();
        } catch (IllegalStateException | IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * Prepares a runtime copy of {@code card} cast with the chosen modes: their effects replace the
     * modal, their targets are declared, and a split half takes that half's characteristics.
     */
    public PreparedModalCast prepareModalCast(GameData gameData, Card card, UUID controllerId,
                                               ChooseOneEffect modal, List<Integer> chosenModeIndices) {
        return prepareModalCast(gameData, card, controllerId, modal, chosenModeIndices, null);
    }

    /**
     * As {@link #prepareModalCast(GameData, Card, UUID, ChooseOneEffect, List)}, for a cast that says
     * whether it is from the owner's hand, where a split card with fuse may cast both halves.
     * {@code null} infers it from whether {@code card} is still in the caster's hand.
     */
    public PreparedModalCast prepareModalCast(GameData gameData, Card card, UUID controllerId,
                                               ChooseOneEffect modal, List<Integer> chosenModeIndices,
                                               Boolean castFromOwnHand) {
        return prepareModalCast(gameData, card, controllerId, modal, chosenModeIndices, castFromOwnHand,
                modal.choicesMax());
    }

    /**
     * As {@link #prepareModalCast(GameData, Card, UUID, ChooseOneEffect, List, Boolean)}, for a cast
     * that may choose up to {@code maximumChoices} modes, more than the card's own modal allows.
     */
    public PreparedModalCast prepareModalCast(GameData gameData, Card card, UUID controllerId,
                                               ChooseOneEffect modal, List<Integer> chosenModeIndices,
                                               Boolean castFromOwnHand, int maximumChoices) {
        maximumChoices = Math.max(maximumChoices, modal.choicesMax());
        ChooseOneEffect castModal = maximumChoices == modal.choicesMax() ? modal : new ChooseOneEffect(
                modal.options(), modal.optional(), modal.choicesRequired(), maximumChoices,
                modal.allModesWhenOptionalCostPaid(), modal.modesMayRepeat(), null,
                modal.modeCosts(), modal.modeBudget(), modal.choicesEqualModalXValue());
        int[] indices = chosenModeIndices.stream().mapToInt(Integer::intValue).toArray();
        int modeEncoding = modal.modesMayRepeat()
                ? castModal.encodeRepeatedSelection(indices)
                : ChooseOneEffect.encodeModeSelection(modal.choicesRequired(), maximumChoices, indices);

        Card runtimeCard = card.createRuntimeCopy();
        List<CardEffect> effects = new ArrayList<>(runtimeCard.getEffects(EffectSlot.SPELL));
        if (castModal != modal) {
            int modalIndex = effects.indexOf(modal);
            if (modalIndex >= 0) {
                effects.set(modalIndex, castModal);
            }
        }
        additionalSpellCostService.extractAndRemove(gameData, controllerId, runtimeCard, effects, false);
        int effectiveXValue = spellCastingService.prepareModalSpellCast(
                gameData, controllerId, runtimeCard, effects, modeEncoding, castFromOwnHand);
        return new PreparedModalCast(runtimeCard, effects, effectiveXValue);
    }

    private void castPreparedSpell(GameData gameData, UUID playerId, Card physicalCard,
                                   Card cardToCast, List<CardEffect> spellEffects,
                                   StackEntryType spellType, boolean asCopy, int effectiveXValue) {
        String playerName = gameData.playerIdToName.get(playerId);
        boolean castWithAdventure = !asCopy && cardToCast != physicalCard
                && physicalCard.getBackFaceCard() != null
                && physicalCard.getCastingOption(AdventureCast.class).isPresent();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(physicalCard.getId());
        UUID ownerId = exiledEntry == null ? physicalCard.getOwnerId() : exiledEntry.ownerId();

        if (needsCastTarget(cardToCast, spellEffects)) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(
                    gameData, cardToCast, spellEffects, playerId);
            boolean multiTarget = cardToCast.getMaxTargets() > 1;
            boolean hasLegalTargets = multiTarget
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, cardToCast, playerId)
                    : !firstCandidates.isEmpty();

            if (!hasLegalTargets) {
                if (asCopy) {
                    gameData.removeFromExile(physicalCard.getId());
                }
                boolean willGoToGraveyard = gameData.pendingExileFreeCastRemainderToGraveyard
                        .contains(physicalCard.getId());
                boolean willGoToBottom = gameData.pendingExileFreeCastRemainderToLibraryBottom
                        .contains(physicalCard.getId());
                boolean willGoToHand = gameData.pendingExileFreeCastRemainderToHand
                        .contains(physicalCard.getId());
                gameLogService.append(gameData, GameLog.cardThen(physicalCard, willGoToGraveyard
                        ? " has no valid targets and will be put into the graveyard."
                        : willGoToBottom ? " has no valid targets and will be put on the bottom of its owner's library."
                        : willGoToHand ? " has no valid targets and will be put into its owner's hand."
                        : asCopy ? " has no valid targets."
                        : " has no valid targets and stays exiled."));
                castNextFromQueue(gameData, playerId);
                return;
            }

            if (asCopy) {
                // A copy exists only as the spell being cast, so it leaves exile as casting begins; it is
                // carried by the target-choice context from here on.
                gameData.removeFromExile(physicalCard.getId());
            }
            gameData.interaction.setPermanentChoiceContext(castWithAdventure
                    ? new PermanentChoiceContext.ExileCastSpellTarget(
                            cardToCast, playerId, spellEffects, spellType, false, List.of(), 0, false, 0,
                            false, false, null, null, 0, physicalCard)
                    : new PermanentChoiceContext.ExileCastSpellTarget(
                            cardToCast, playerId, spellEffects, spellType, asCopy));
            playerInputService.beginPermanentChoice(gameData, playerId, firstCandidates,
                    "Choose a target for " + physicalCard.getName() + ".");
            gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", physicalCard,
                    " without paying its mana cost — choosing target."));
            return;
        }

        StackEntry entry = new StackEntry(
                spellType, cardToCast, playerId, cardToCast.getName(),
                spellEffects, effectiveXValue, (UUID) null, null
        );
        entry.setCopy(asCopy);
        entry.setCastCopy(asCopy);
        if (castWithAdventure) {
            entry.setPhysicalCard(physicalCard);
            entry.setCastWithAdventure(true);
        }
        if (!asCopy) {
            entry.setSourceZone(Zone.EXILE);
            entry.setOwnerIdOverride(ownerId);
        }
        if (beginSacrificeCostIfNeeded(gameData, entry)) return;
        if (gameData.removeFromExile(physicalCard.getId()) && !asCopy) {
            gameData.recordCardPlayedFromExile(playerId);
        }
        gameData.stack.add(entry);
        gameData.recordSpellCast(playerId, cardToCast);
        gameData.priorityPassedBy.clear();
        gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", physicalCard,
                " without paying its mana cost."));
        triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, playerId, Zone.EXILE);
        if (asCopy && spellweaverVoluteSupport.handleSuccessfulCopyCast(gameData, physicalCard.getId())) {
            return;
        }
        castNextFromQueue(gameData, playerId);
    }

    /** Pauses a prepared free cast for a required creature sacrifice, after target selection. */
    public boolean beginSacrificeCostIfNeeded(GameData gameData, StackEntry entry) {
        Integer escalateModes = gameData.freeCastEscalateModeCounts.remove(entry.getCard().getId());
        if (escalateModes != null) {
            return beginEscalateCost(gameData, entry, escalateModes);
        }
        List<CardEffect> costs = entry.getEffectsToResolve().stream().filter(CostEffect.class::isInstance).toList();
        if (costs.size() == 1 && costs.getFirst() instanceof com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost) {
            List<UUID> candidates = spellCastingService.beholdCostCandidates(gameData, entry);
            if (candidates.isEmpty()) throw new IllegalStateException("No object can pay the behold cost");
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.FreeCastBeholdCost(entry));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates,
                    "Choose a permanent or hand card to behold for " + entry.getCard().getName() + ".");
            return true;
        }
        if (costs.size() == 1 && costs.stream().allMatch(cost -> cost instanceof DiscardCardTypeCost
                || cost instanceof com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost)) {
            var discardOrPay = costs.stream()
                    .filter(com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost.class::isInstance)
                    .map(com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost.class::cast)
                    .findFirst().orElse(null);
            List<Integer> candidates = additionalSpellCostService.validDiscardCostIndices(
                    gameData, entry.getControllerId(), entry.getCard());
            boolean canPayMana = discardOrPay != null && new com.github.laxika.magicalvibes.model.ManaCost(
                    discardOrPay.manaCost()).canPay(gameData.playerManaPools.get(entry.getControllerId()));
            if (canPayMana && candidates != null && !candidates.isEmpty()) {
                interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                        entry.getControllerId(), null, null, new ChoiceContext.FreeCastAdditionalCostChoice(entry),
                        List.of("Discard a card", "Pay " + discardOrPay.manaCost()),
                        "Choose how to pay the additional cost."));
                return true;
            }
            if (canPayMana) {
                spellCastingService.payAdditionalCostsForFreeCast(gameData, entry,
                        AdditionalSpellCostService.CostSelection.none());
                return false;
            }
            beginFreeCastDiscard(gameData, entry);
            return true;
        }
        if (costs.size() == 1 && costs.getFirst() instanceof DiscardCardOrPayLifeCost discardOrPayLife) {
            List<Integer> discardCandidates = additionalSpellCostService.validDiscardCostIndices(
                    gameData, entry.getControllerId(), entry.getCard());
            boolean canDiscard = discardCandidates != null && !discardCandidates.isEmpty();
            boolean canPayLife = gameQueryService.canPayLifeForCosts(gameData)
                    && gameData.getLife(entry.getControllerId()) >= discardOrPayLife.lifeAmount();
            if (canDiscard && canPayLife) {
                interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                        entry.getControllerId(), null, null, new ChoiceContext.FreeCastAdditionalCostChoice(entry),
                        List.of("Discard a card", "Pay " + discardOrPayLife.lifeAmount() + " life"),
                        "Choose how to pay the additional cost."));
                return true;
            }
            if (canDiscard) {
                beginFreeCastDiscard(gameData, entry);
                return true;
            }
            if (askFreeCastLifeCostFirst(gameData, entry, discardOrPayLife.lifeAmount(), true)) {
                return true;
            }
            spellCastingService.payAdditionalCostsForFreeCast(gameData, entry,
                    AdditionalSpellCostService.CostSelection.none());
            return false;
        }
        if (costs.size() == 1 && costs.getFirst() instanceof PutCountersOnControlledCreatureOrPayManaCost counterCost) {
            // The counter-or-mana additional cost (Bogslither's Embrace) is still paid when the spell is
            // cast without paying its mana cost; only the option's own mana can be spent on it.
            List<UUID> candidates = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())
                    .stream()
                    .map(com.github.laxika.magicalvibes.model.Permanent::getId)
                    .filter(id -> canPutCountersAsFreeCastCost(gameData, entry, id))
                    .toList();
            boolean canPayMana = new com.github.laxika.magicalvibes.model.ManaCost(counterCost.manaCost())
                    .canPay(gameData.playerManaPools.get(entry.getControllerId()));
            if (canPayMana && !candidates.isEmpty()) {
                interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                        entry.getControllerId(), null, null, new ChoiceContext.FreeCastAdditionalCostChoice(entry),
                        List.of(COUNTERS_OPTION, "Pay " + counterCost.manaCost()),
                        "Choose how to pay the additional cost."));
                return true;
            }
            if (canPayMana) {
                spellCastingService.payAdditionalCostsForFreeCast(gameData, entry,
                        AdditionalSpellCostService.CostSelection.none());
                return false;
            }
            beginFreeCastCounters(gameData, entry, candidates);
            return true;
        }
        if (costs.size() == 1 && costs.getFirst() instanceof TapMultiplePermanentsCost tapCost) {
            List<UUID> candidates = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())
                    .stream()
                    .filter(permanent -> !permanent.isTapped()
                            && predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, tapCost.filter()))
                    .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
            // A spell cast without paying its mana cost announces X = 0.
            int required = tapCost.count() instanceof com.github.laxika.magicalvibes.model.amount.Fixed fixed
                    ? fixed.value() : 0;
            if (candidates.size() < required) {
                throw new IllegalStateException("Not enough untapped permanents to pay the tap cost");
            }
            playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), candidates,
                    required, new MultiPermanentChoiceContext.FreeCastTapCost(entry, required),
                    "Choose " + required + " untapped permanents to tap to cast "
                            + entry.getCard().getName() + ".");
            return true;
        }
        if (costs.size() == 1 && costs.getFirst() instanceof SacrificePermanentOrDiscardCardCost sacrificeOrDiscard) {
            List<Integer> discardCandidates = additionalSpellCostService.validDiscardCostIndices(
                    gameData, entry.getControllerId(), entry.getCard());
            boolean canDiscard = discardCandidates != null && !discardCandidates.isEmpty();
            List<UUID> sacrificeCandidates = sacrificeOrDiscardCandidates(gameData, entry, sacrificeOrDiscard);
            if (canDiscard && !sacrificeCandidates.isEmpty()) {
                interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                        entry.getControllerId(), null, null, new ChoiceContext.FreeCastAdditionalCostChoice(entry),
                        List.of("Discard a card", "Sacrifice " + sacrificeOrDiscard.description()),
                        "Choose how to pay the additional cost."));
                return true;
            }
            if (canDiscard) {
                beginFreeCastDiscard(gameData, entry);
                return true;
            }
            beginFreeCastSacrifice(gameData, entry, sacrificeCandidates, sacrificeOrDiscard.description());
            return true;
        }
        if (costs.size() == 1 && costs.getFirst() instanceof ReturnPermanentToHandCost returnCost) {
            // "As an additional cost to cast this spell, return a land you control to its owner's hand"
            // (Devour in Flames) still has to be paid when the spell is cast without paying its mana cost.
            List<UUID> candidates = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, returnCost.filter()))
                    .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
            if (candidates.isEmpty()) throw new IllegalStateException("Nothing can pay the return-to-hand cost");
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.FreeCastSacrificeCost(entry));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates,
                    "Choose a permanent to return to its owner's hand to cast " + entry.getCard().getName() + ".");
            return true;
        }
        if (costs.size() == 1 && costs.getFirst() instanceof SacrificePermanentCost sacrificeCost
                && !sacrificeCost.optional()) {
            List<UUID> candidates = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                    .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, sacrificeCost.filter())
                            && gameQueryService.canChooseToSacrificeForCosts(gameData, permanent)
                            && !(sacrificeCost.excludeSource()
                            && permanent.getCard().getId().equals(entry.getCard().getId())))
                    .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
            beginFreeCastSacrifice(gameData, entry, candidates, sacrificeCost.description());
            return true;
        }
        if (entry.getEffectsToResolve().stream().anyMatch(SacrificeAllCreaturesYouControlCost.class::isInstance)) {
            if (spellCastingService.askFreeCastSacrificeAllFirst(gameData, entry)) {
                // CR 616.1: a creature it sacrifices needs its death replacement chosen first. The cast is paused
                // like a chosen sacrifice and completed as one, with no permanent chosen, once every answer is in.
                gameData.pendingDeathReplacementChoiceReplay = new GameData.PermanentChoiceReplay(
                        new com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice(
                                entry.getControllerId(), List.of(), List.of(),
                                new PermanentChoiceContext.FreeCastSacrificeCost(entry), ""), null);
                return true;
            }
            spellCastingService.payAdditionalCostsForFreeCast(gameData, entry,
                    AdditionalSpellCostService.CostSelection.none());
            return false;
        }
        boolean needsSacrifice = entry.getEffectsToResolve().stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost.class::isInstance);
        if (!needsSacrifice) return false;
        List<UUID> candidates = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeChosenToSacrifice(gameData, permanent)
                        && entry.getEffectsToResolve().stream()
                        .filter(com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost.class::isInstance)
                        .map(com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost.class::cast)
                        .noneMatch(cost -> cost.excludeSelf() && permanent.getCard().getId().equals(entry.getCard().getId())))
                .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
        if (candidates.isEmpty()) throw new IllegalStateException("No creature can pay the sacrifice cost");
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.FreeCastSacrificeCost(entry));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates,
                "Choose a creature to sacrifice to cast " + entry.getCard().getName() + ".");
        return true;
    }

    /**
     * The most modes a free cast of an escalate spell may choose: one, plus one for each escalate
     * payment the caster can still make (CR 702.120a). The spell is cast from exile, so every card in
     * the caster's hand can be discarded.
     */
    private int maxEscalateModes(GameData gameData, UUID playerId, Card card,
                                 AdditionalSpellCostService.ExtractedCosts costs, int choicesMax) {
        int modes = 1;
        while (modes < choicesMax && canPayEscalate(gameData, playerId, card, costs, modes + 1)) {
            modes++;
        }
        return modes;
    }

    private boolean canPayEscalate(GameData gameData, UUID playerId, Card card,
                                   AdditionalSpellCostService.ExtractedCosts costs, int modes) {
        int payments = modes - 1;
        if (costs.escalateDiscardCost() != null) {
            return gameData.playerHands.getOrDefault(playerId, List.of()).size() >= payments;
        }
        if (costs.escalateManaCost() != null) {
            return spellCastingService.canPayFreeCastEscalateMana(
                    gameData, playerId, card, costs.escalateManaCost(), modes);
        }
        if (costs.escalateSacrificeCost() != null) {
            return escalateSacrificeCandidates(gameData, playerId, costs.escalateSacrificeCost()).size()
                    >= costs.escalateSacrificeCost().count() * payments;
        }
        if (costs.escalateTapCost() != null) {
            return escalateTapCandidates(gameData, playerId, costs.escalateTapCost()).size() >= payments;
        }
        return false;
    }

    private List<UUID> escalateSacrificeCandidates(GameData gameData, UUID playerId,
                                                   com.github.laxika.magicalvibes.model.effect.EscalateSacrificeCost cost) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, cost.filter())
                        && gameQueryService.canChooseToSacrificeForCosts(gameData, permanent))
                .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
    }

    private List<UUID> escalateTapCandidates(GameData gameData, UUID playerId,
                                             com.github.laxika.magicalvibes.model.effect.EscalateTapCost cost) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !permanent.isTapped()
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, cost.filter()))
                .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
    }

    /**
     * Pays the escalate cost of a free cast whose caster chose {@code modes} modes, after its targets
     * were chosen (CR 601.2h). Mana comes from the caster's mana pool; a discard, sacrifice or tap
     * pauses for the caster to choose what pays it. Returns whether the cast is paused.
     */
    private boolean beginEscalateCost(GameData gameData, StackEntry entry, int modes) {
        UUID playerId = entry.getControllerId();
        Card card = entry.getCard();
        entry.setModalModeCount(modes);
        AdditionalSpellCostService.ExtractedCosts costs = additionalSpellCostService.peek(gameData, playerId, card);
        int payments = modes - 1;
        if (costs.escalateManaCost() != null) {
            spellCastingService.payFreeCastEscalateMana(gameData, entry, costs.escalateManaCost(), modes);
            return false;
        }
        if (costs.escalateDiscardCost() != null) {
            List<Integer> candidates = new ArrayList<>();
            for (int i = 0; i < gameData.playerHands.getOrDefault(playerId, List.of()).size(); i++) {
                candidates.add(i);
            }
            if (candidates.size() < payments) throw new IllegalStateException("Not enough cards to pay escalate");
            playerInputService.beginDiscardChoice(gameData, playerId, candidates,
                    "Choose " + payments + (payments == 1 ? " card" : " cards") + " to discard to escalate "
                            + card.getName() + ".", payments, DiscardFollowUp.NONE.withPendingSpellCast(entry));
            return true;
        }
        List<UUID> candidates;
        int required;
        CostEffect cost;
        String verb;
        if (costs.escalateSacrificeCost() != null) {
            cost = costs.escalateSacrificeCost();
            candidates = escalateSacrificeCandidates(gameData, playerId, costs.escalateSacrificeCost());
            required = costs.escalateSacrificeCost().count() * payments;
            verb = "sacrifice";
        } else if (costs.escalateTapCost() != null) {
            cost = costs.escalateTapCost();
            candidates = escalateTapCandidates(gameData, playerId, costs.escalateTapCost());
            required = payments;
            verb = "tap";
        } else {
            return false;
        }
        if (candidates.size() < required) throw new IllegalStateException("Not enough permanents to pay escalate");
        List<CardEffect> effects = new ArrayList<>(entry.getEffectsToResolve());
        effects.add(cost);
        entry.replaceEffectsToResolve(effects);
        playerInputService.beginMultiPermanentChoice(gameData, playerId, candidates, required,
                new MultiPermanentChoiceContext.FreeCastTapCost(entry, required),
                "Choose " + required + " permanents to " + verb + " to escalate " + card.getName() + ".");
        return true;
    }

    private boolean canPutCountersAsFreeCastCost(GameData gameData, StackEntry entry, UUID permanentId) {
        PutCountersOnControlledCreatureOrPayManaCost cost = entry.getEffectsToResolve().stream()
                .filter(PutCountersOnControlledCreatureOrPayManaCost.class::isInstance)
                .map(PutCountersOnControlledCreatureOrPayManaCost.class::cast)
                .findFirst().orElseThrow();
        try {
            additionalSpellCostService.validatePutCountersOnControlledCreatureOrPayManaCost(gameData,
                    new Player(entry.getControllerId(), gameData.playerIdToName.get(entry.getControllerId())),
                    entry.getCard(), cost, permanentId);
            return true;
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    private void beginFreeCastCounters(GameData gameData, StackEntry entry, List<UUID> candidates) {
        if (candidates.isEmpty()) throw new IllegalStateException("Nothing can pay the counter cost");
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.FreeCastSacrificeCost(entry));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates,
                "Choose a creature you control to put counters on to cast " + entry.getCard().getName() + ".");
    }

    private List<UUID> sacrificeOrDiscardCandidates(GameData gameData, StackEntry entry,
                                                     SacrificePermanentOrDiscardCardCost cost) {
        return gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, cost.filter())
                        && gameQueryService.canChooseToSacrificeForCosts(gameData, permanent))
                .map(com.github.laxika.magicalvibes.model.Permanent::getId).toList();
    }

    private void beginFreeCastSacrifice(GameData gameData, StackEntry entry, List<UUID> candidates,
                                        String description) {
        if (candidates.isEmpty()) throw new IllegalStateException("Nothing can pay the sacrifice cost");
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.FreeCastSacrificeCost(entry));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates,
                "Choose " + description + " to sacrifice to cast " + entry.getCard().getName() + ".");
    }

    private void beginFreeCastDiscard(GameData gameData, StackEntry entry) {
        List<Integer> candidates = additionalSpellCostService.validDiscardCostIndices(
                gameData, entry.getControllerId(), entry.getCard());
        if (candidates == null || candidates.isEmpty()) throw new IllegalStateException("No card can pay the discard cost");
        int count = entry.getEffectsToResolve().stream().filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast).mapToInt(DiscardCardTypeCost::count).sum();
        if (count == 0) count = 1;
        entry.replaceEffectsToResolve(entry.getEffectsToResolve().stream()
                .filter(effect -> !(effect instanceof DiscardCardTypeCost)
                        && !(effect instanceof SacrificePermanentOrDiscardCardCost)
                        && !(effect instanceof DiscardCardOrPayLifeCost)
                        && !(effect instanceof com.github.laxika.magicalvibes.model.effect.DiscardCardOrPayManaCost)).toList());
        playerInputService.beginDiscardChoice(gameData, entry.getControllerId(), candidates,
                "Choose a card to discard to cast " + entry.getCard().getName() + ".", count,
                DiscardFollowUp.NONE.withPendingSpellCast(entry));
    }

    /** Resumes a prepared spell after choosing its required discard-or-mana payment. */
    public void completeAdditionalCostChoice(GameData gameData, String choice,
                                              ChoiceContext.FreeCastAdditionalCostChoice context) {
        gameData.interaction.clearAwaitingInput();
        if (choice.equals("Discard a card")) {
            beginFreeCastDiscard(gameData, context.entry());
        } else if (choice.equals(COUNTERS_OPTION)) {
            beginFreeCastCounters(gameData, context.entry(), gameData.playerBattlefields
                    .getOrDefault(context.entry().getControllerId(), List.of()).stream()
                    .map(com.github.laxika.magicalvibes.model.Permanent::getId)
                    .filter(id -> canPutCountersAsFreeCastCost(gameData, context.entry(), id))
                    .toList());
        } else if (context.entry().getEffectsToResolve().stream()
                .anyMatch(SacrificePermanentOrDiscardCardCost.class::isInstance)) {
            SacrificePermanentOrDiscardCardCost cost = context.entry().getEffectsToResolve().stream()
                    .filter(SacrificePermanentOrDiscardCardCost.class::isInstance)
                    .map(SacrificePermanentOrDiscardCardCost.class::cast).findFirst().orElseThrow();
            beginFreeCastSacrifice(gameData, context.entry(),
                    sacrificeOrDiscardCandidates(gameData, context.entry(), cost), cost.description());
        } else {
            if (askFreeCastLifeCostFirst(gameData, context.entry(), context.entry().getEffectsToResolve().stream()
                    .filter(DiscardCardOrPayLifeCost.class::isInstance)
                    .mapToInt(cost -> ((DiscardCardOrPayLifeCost) cost).lifeAmount())
                    .sum(), false)) {
                return;
            }
            spellCastingService.payAdditionalCostsForFreeCast(gameData, context.entry(),
                    AdditionalSpellCostService.CostSelection.none());
            completeCastAfterDiscard(gameData, context.entry());
        }
    }

    /**
     * CR 601.2h and CR 616.1: before a prepared free cast pays {@code lifeCost} life for its "discard a card or pay
     * life" cost, asks the death replacement questions of the Lich's Duel Mastery that payment would sacrifice instead
     * (paying life is losing it, CR 119.4). The cast is made while something resolves, so it is paused, not stopped
     * and made again, and is completed with the life paid once every answer is in
     * ({@link #completeLifeCostAfterAnswers}); nothing is asked inside a rerun window, whose completion asks as the
     * Mastery leaves.
     *
     * @return whether the cast paused to ask
     */
    private boolean askFreeCastLifeCostFirst(GameData gameData, StackEntry entry, int lifeCost,
                                             boolean checksTargetTriggers) {
        if (gameData.deathReplacementsCanRerunEffect || lifeCost <= 0) {
            return false;
        }
        List<com.github.laxika.magicalvibes.model.DepartureIntent> sacrifices =
                lifeSupport.successiveLifeLossSacrifices(gameData, entry.getControllerId(), 1);
        if (sacrifices.isEmpty()) {
            return false;
        }
        UUID previousCauseControllerId = gameData.currentlyResolvingControllerId;
        boolean asked;
        try {
            gameData.currentlyResolvingControllerId = entry.getControllerId();
            asked = departureService.askBeforePayment(gameData,
                    com.github.laxika.magicalvibes.model.DepartureBatch.of(sacrifices));
        } finally {
            gameData.currentlyResolvingControllerId = previousCauseControllerId;
        }
        if (asked) {
            gameData.pendingDeathReplacementChoiceReplay =
                    new GameData.FreeCastLifeCostReplay(entry, checksTargetTriggers);
        }
        return asked;
    }

    /**
     * Pays the life cost of the free cast {@link #askFreeCastLifeCostFirst} paused and finishes the cast, once every
     * death replacement it asked about is chosen.
     */
    public void completeLifeCostAfterAnswers(GameData gameData, GameData.FreeCastLifeCostReplay replay) {
        spellCastingService.payAdditionalCostsForFreeCast(gameData, replay.entry(),
                AdditionalSpellCostService.CostSelection.none());
        // The life payment was asked its Mastery's death replacements first (CR 616.1); nothing after it is redone.
        gameData.deathReplacementsCanRerunEffect = false;
        completeCastAfterDiscard(gameData, replay.entry());
        if (replay.checksTargetTriggers()) {
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        }
    }

    public void completeSacrificeCost(GameData gameData, UUID permanentId,
                                      PermanentChoiceContext.FreeCastSacrificeCost context) {
        spellCastingService.paySacrificeCreatureCostForFreeCast(gameData, context.entry(), permanentId);
        // The sacrifice was asked its death replacements first (CR 616.1); nothing after it is redone.
        gameData.deathReplacementsCanRerunEffect = false;
        completeCastAfterDiscard(gameData, context.entry());
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
    }

    public void completeTapCost(GameData gameData, List<UUID> permanentIds,
                                MultiPermanentChoiceContext.FreeCastTapCost context) {
        spellCastingService.payAdditionalCostsForFreeCast(gameData, context.entry(),
                new AdditionalSpellCostService.CostSelection(null, null, null, null, null,
                        Objects.requireNonNullElse(context.entry().getModalModeCount(), 0), -1,
                        List.copyOf(permanentIds), null, null, List.of(), List.of()));
        // An escalate sacrifice was asked its death replacements first (CR 616.1); nothing after it is redone.
        gameData.deathReplacementsCanRerunEffect = false;
        completeCastAfterDiscard(gameData, context.entry());
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
    }

    public void completeBeholdCost(GameData gameData, UUID objectId,
                                  PermanentChoiceContext.FreeCastBeholdCost context) {
        spellCastingService.payBeholdCostForPreparedCast(gameData, context.entry(), objectId);
        completeCastAfterDiscard(gameData, context.entry());
        triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
    }

    /** Adds a spell to the stack only after its queued discard cost has been paid. */
    public void completeCastAfterDiscard(GameData gameData, StackEntry entry) {
        UUID playerId = entry.getControllerId();
        boolean leftExile = gameData.removeFromExile(entry.getCard().getId());
        if (!entry.isCopy() && leftExile) gameData.recordCardPlayedFromExile(playerId);
        gameData.stack.add(entry);
        gameData.recordSpellCast(playerId, entry.getCard());
        gameData.priorityPassedBy.clear();
        triggerCollectionService.checkSpellCastTriggers(gameData, entry.getCard(), playerId,
                entry.getSourceZone() != null ? entry.getSourceZone() : Zone.HAND, null);
        if (entry.isCopy() && spellweaverVoluteSupport.handleSuccessfulCopyCast(
                gameData, entry.getCard().getId())) return;
        castNextFromQueue(gameData, playerId);
    }

    private boolean needsCastTarget(Card card, List<CardEffect> spellEffects) {
        return EffectResolution.needsSpellCastTarget(spellEffects,
                card.isAuraThatRequiresAttachment(), card.isEnchantPlayer())
                || EffectResolution.needsSpellTarget(spellEffects);
    }

    private void skipCardWithUnsupportedCost(GameData gameData, UUID playerId, Card card, boolean asCopy) {
        if (asCopy) {
            gameData.removeFromExile(card.getId());
        }
        boolean willGoToBottom = gameData.pendingExileFreeCastRemainderToLibraryBottom.contains(card.getId());
        boolean willGoToHand = gameData.pendingExileFreeCastRemainderToHand.contains(card.getId());
        gameLogService.append(gameData, GameLog.cardThen(card, asCopy
                ? " has an additional cast cost that can't be paid and ceases to exist."
                : willGoToBottom ? " has an additional cast cost that can't be paid and will be put on the bottom of its owner's library."
                : willGoToHand ? " has an additional cast cost that can't be paid and will be put into its owner's hand."
                : " has an additional cast cost that can't be paid and stays exiled."));
        castNextFromQueue(gameData, playerId);
    }

    private void skipCardWithoutLegalMode(GameData gameData, UUID playerId, Card card, boolean asCopy) {
        if (asCopy) {
            gameData.removeFromExile(card.getId());
        }
        boolean willGoToBottom = gameData.pendingExileFreeCastRemainderToLibraryBottom.contains(card.getId());
        boolean willGoToHand = gameData.pendingExileFreeCastRemainderToHand.contains(card.getId());
        gameLogService.append(gameData, GameLog.cardThen(card, asCopy
                ? " has no legal mode and ceases to exist."
                : willGoToBottom ? " has no legal mode and will be put on the bottom of its owner's library."
                : willGoToHand ? " has no legal mode and will be put into its owner's hand."
                : " has no legal mode and stays exiled."));
        castNextFromQueue(gameData, playerId);
    }

    /** A modal spell ready for the stack: its runtime card, chosen effects and X value. */
    public record PreparedModalCast(Card card, List<CardEffect> effects, int effectiveXValue) {
    }

    /**
     * Ends the free-cast process. The shared input epilogue resumes the entry parked in
     * {@code GameData.pendingEffectResolutionEntry} — right when the queue was driven from an input
     * handler, wrong when it ran inside a live effect-resolution frame (a cipher trigger's "you may
     * cast a copy", CR 702.99a). That frame still sits on the parked index, so re-entering it re-runs
     * the same {@code MayEffect} and prompts a second time — casting a second copy if accepted. The
     * active frame completes the resolution itself, so skip the epilogue while one is running.
     */
    /**
     * The mana value the spell being cast may have under a shared total limit: what is left after
     * the earlier casts, keeping enough for the cheapest half of every spell still queued
     * (CR 709.3a). Unlimited when the casts share no limit.
     */
    private static int availableManaValue(GameData gameData) {
        if (gameData.pendingFreeCastManaValueBudget == null) {
            return Integer.MAX_VALUE;
        }
        int reserved = gameData.pendingFreeCastQueue.stream()
                .map(gameData::findExiledCard)
                .filter(Objects::nonNull)
                .mapToInt(exiled -> exiled.card().getSmallestSpellManaValue())
                .sum();
        return gameData.pendingFreeCastManaValueBudget - reserved;
    }

    private static void spendManaValueBudget(GameData gameData, int manaValue) {
        if (gameData.pendingFreeCastManaValueBudget != null) {
            gameData.pendingFreeCastManaValueBudget -= manaValue;
        }
    }

    /**
     * Kefka, Dancing Mad's "Then each player who owns a spell you cast this way loses life equal to its mana value":
     * the losses follow the casts, together, as a step of their own of the parked resolution (CR 608.2c), so a Lich's
     * Duel Mastery sacrifice replacing one has its death replacements chosen before any life is lost (CR 616.1).
     */
    private void applyPendingFreeCastLifeLosses(GameData gameData) {
        List<PendingFreeCastLifeLoss> pending = new ArrayList<>(gameData.pendingFreeCastLifeLosses);
        gameData.pendingFreeCastLifeLosses.clear();
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        String sourceName = null;
        for (PendingFreeCastLifeLoss loss : pending) {
            if (gameData.findExiledCard(loss.cardId()) == null) {
                losses.merge(loss.ownerId(), loss.manaValue(), Integer::sum);
                sourceName = loss.sourceName();
            }
        }
        lifeSupport.loseLifeAsStepOfParkedResolution(gameData, losses, sourceName);
    }

    private void finishFreeCastProcess(GameData gameData) {
        applyPendingFreeCastLifeLosses(gameData);
        gameData.pendingFreeCastManaValueBudget = null;
        gameData.pendingFreeCastLandPlayIds.clear();
        gameData.pendingFreeCastAdventureIds.clear();
        gameData.freeCastEscalateModeCounts.clear();
        spellweaverVoluteSupport.clearIfUncast(gameData);
        putRemainderIntoOwnersGraveyards(gameData);
        putRemainderIntoLibraryBottom(gameData);
        putRemainderIntoOwnersHands(gameData);
        if (gameData.effectResolutionDepth > 0 && gameData.pendingEffectResolutionEntry != null) {
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
