package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
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
import java.util.List;
import java.util.Map;
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
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        if (exiledEntry == null) {
            castNextFromQueue(gameData, playerId);
            return;
        }

        Card card = exiledEntry.card();
        if (card.hasType(CardType.LAND) && gameData.pendingEffectResolutionEntry != null
                && gameData.pendingEffectResolutionEntry.getCard().getEffects(EffectSlot.SPELL).stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.BrilliantUltimatumEffect.class::isInstance)) {
            brilliantUltimatumSupport.playLandFromExile(gameData, playerId, card);
            if (!gameData.interaction.isAwaitingInput()) castNextFromQueue(gameData, playerId);
            return;
        }
        if (card.isCastOnlyFromGraveyard()
                || !castingPermissionService.isSpellCastingAllowed(gameData, playerId, card)) {
            if (asCopy) {
                gameData.removeFromExile(cardId);
            }
            gameLogService.append(gameData, GameLog.cardThen(card,
                    asCopy ? " cannot be cast from exile." : " cannot be cast from exile and stays exiled."));
            castNextFromQueue(gameData, playerId);
            return;
        }
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
        Card cardToCast = asCopy
                && !card.hasType(CardType.INSTANT)
                && !card.hasType(CardType.SORCERY)
                ? copySupport.createTokenCopyCard(card)
                : card;
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
                    ? Math.min(1, modal.choicesMax())
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

        if (additionalCosts.any()) {
            if (spellEffects.stream().filter(CostEffect.class::isInstance).count() == 1
                    && (additionalCosts.discardCardOrPayManaCost() != null || additionalCosts.discardCost() != null)
                    && additionalSpellCostService.satisfiableWithoutManaCost(gameData, playerId, cardToCast)) {
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
            skipCardWithUnsupportedCost(gameData, playerId, card, asCopy);
            return;
        }

        castPreparedSpell(gameData, playerId, card, cardToCast, spellEffects, spellType, asCopy, 0);
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
                chosenModeIndices, offeredModes, context.maximumChoices(), context.copy());
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
        List<String> labels = chosenModeIndices.stream()
                .map(context.effect().options()::get)
                .map(ChooseOneEffect.ChooseOneOption::label)
                .toList();
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses " + labels + " for ", prepared.card(), "."));
        castPreparedSpell(gameData, context.controllerId(), context.cardToCast(), prepared.card(),
                prepared.effects(), context.spellType(), context.copy(), prepared.effectiveXValue());
    }

    private List<Integer> legalNextModeIndices(GameData gameData, Card card, UUID controllerId,
                                               ChooseOneEffect modal, List<Integer> chosenModeIndices,
                                               int maximumChoices) {
        List<Integer> legal = new ArrayList<>();
        for (int modeIndex = 0; modeIndex < modal.options().size(); modeIndex++) {
            if (!modal.modesMayRepeat() && chosenModeIndices.contains(modeIndex)) {
                continue;
            }
            List<Integer> trial = new ArrayList<>(chosenModeIndices);
            trial.add(modeIndex);
            if (trial.size() < modal.choicesRequired()
                    || isLegalModalSelection(gameData, card, controllerId, modal, trial)) {
                legal.add(modeIndex);
            }
        }
        return legal;
    }

    private boolean isLegalModalSelection(GameData gameData, Card card, UUID controllerId,
                                          ChooseOneEffect modal, List<Integer> chosenModeIndices) {
        try {
            PreparedModalCast prepared = prepareModalCast(
                    gameData, card, controllerId, modal, chosenModeIndices);
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

    private PreparedModalCast prepareModalCast(GameData gameData, Card card, UUID controllerId,
                                               ChooseOneEffect modal, List<Integer> chosenModeIndices) {
        int[] indices = chosenModeIndices.stream().mapToInt(Integer::intValue).toArray();
        int modeEncoding = modal.modesMayRepeat()
                ? modal.encodeRepeatedSelection(indices)
                : ChooseOneEffect.encodeModeSelection(modal.choicesRequired(), modal.choicesMax(), indices);

        Card runtimeCard = card.createRuntimeCopy();
        List<CardEffect> effects = new ArrayList<>(runtimeCard.getEffects(EffectSlot.SPELL));
        additionalSpellCostService.extractAndRemove(gameData, controllerId, runtimeCard, effects);
        int effectiveXValue = spellCastingService.prepareModalSpellCast(
                gameData, controllerId, runtimeCard, effects, modeEncoding);
        return new PreparedModalCast(runtimeCard, effects, effectiveXValue);
    }

    private void castPreparedSpell(GameData gameData, UUID playerId, Card physicalCard,
                                   Card cardToCast, List<CardEffect> spellEffects,
                                   StackEntryType spellType, boolean asCopy, int effectiveXValue) {
        String playerName = gameData.playerIdToName.get(playerId);
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

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(
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
        triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, playerId, false);
        if (asCopy && spellweaverVoluteSupport.handleSuccessfulCopyCast(gameData, physicalCard.getId())) {
            return;
        }
        castNextFromQueue(gameData, playerId);
    }

    /** Pauses a prepared free cast for a required creature sacrifice, after target selection. */
    public boolean beginSacrificeCostIfNeeded(GameData gameData, StackEntry entry) {
        List<CardEffect> costs = entry.getEffectsToResolve().stream().filter(CostEffect.class::isInstance).toList();
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
        boolean needsSacrifice = entry.getEffectsToResolve().stream()
                .anyMatch(com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost.class::isInstance);
        if (!needsSacrifice) return false;
        List<UUID> candidates = gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent)
                        && !gameQueryService.cantBeSacrificed(gameData, permanent)
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

    private void beginFreeCastDiscard(GameData gameData, StackEntry entry) {
        List<Integer> candidates = additionalSpellCostService.validDiscardCostIndices(
                gameData, entry.getControllerId(), entry.getCard());
        if (candidates == null || candidates.isEmpty()) throw new IllegalStateException("No card can pay the discard cost");
        int count = entry.getEffectsToResolve().stream().filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast).mapToInt(DiscardCardTypeCost::count).sum();
        if (count == 0) count = 1;
        entry.replaceEffectsToResolve(entry.getEffectsToResolve().stream()
                .filter(effect -> !(effect instanceof DiscardCardTypeCost)
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
        } else {
            spellCastingService.payAdditionalCostsForFreeCast(gameData, context.entry(),
                    AdditionalSpellCostService.CostSelection.none());
            completeCastAfterDiscard(gameData, context.entry());
        }
    }

    public void completeSacrificeCost(GameData gameData, UUID permanentId,
                                      PermanentChoiceContext.FreeCastSacrificeCost context) {
        spellCastingService.paySacrificeCreatureCostForFreeCast(gameData, context.entry(), permanentId);
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

    private record PreparedModalCast(Card card, List<CardEffect> effects, int effectiveXValue) {
    }

    /**
     * Ends the free-cast process. The shared input epilogue resumes the entry parked in
     * {@code GameData.pendingEffectResolutionEntry} — right when the queue was driven from an input
     * handler, wrong when it ran inside a live effect-resolution frame (a cipher trigger's "you may
     * cast a copy", CR 702.99a). That frame still sits on the parked index, so re-entering it re-runs
     * the same {@code MayEffect} and prompts a second time — casting a second copy if accepted. The
     * active frame completes the resolution itself, so skip the epilogue while one is running.
     */
    private void finishFreeCastProcess(GameData gameData) {
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
