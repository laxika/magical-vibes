package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraveyardChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final LifeSupport lifeSupport;
    private final ExileService exileService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InputCompletionService inputCompletionService;
    private final com.github.laxika.magicalvibes.service.effect.EffectResolutionService effectResolutionService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    public void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class) == null) {
            throw new IllegalStateException("Not awaiting graveyard choice");
        }
        PendingInteraction.GraveyardChoice graveyardChoice =
                gameData.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        if (graveyardChoice == null || !player.getId().equals(graveyardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        List<Integer> validIndices = graveyardChoice.validIndices();
        List<Card> cardPool = graveyardChoice.cardPool();

        gameData.interaction.clearAwaitingInput();
        GraveyardChoiceDestination destination = graveyardChoice.destination();
        boolean gainLifeEqualToManaValue = graveyardChoice.gainLifeEqualToManaValue();
        UUID attachToSourcePermanentId = graveyardChoice.attachToSourcePermanentId();
        CardColor grantColor = graveyardChoice.grantColor();
        CardSubtype grantSubtype = graveyardChoice.grantSubtype();
        int exileRemainingCount = graveyardChoice.exileRemainingCount();
        int gainLifeIfCreatureAmount = graveyardChoice.gainLifeIfCreatureAmount();
        UUID gainLifeIfCreaturePlayerId = graveyardChoice.gainLifeIfCreaturePlayerId();
        UUID trackWithSourcePermanentId = graveyardChoice.trackWithSourcePermanentId();
        // May ability graveyard targeting context
        Card mayAbilitySourceCard = graveyardChoice.mayAbilitySourceCard();
        UUID mayAbilityControllerId = graveyardChoice.mayAbilityControllerId();
        java.util.List<CardEffect> mayAbilityEffects = graveyardChoice.mayAbilityEffects();
        UUID mayAbilitySourcePermanentId = graveyardChoice.mayAbilitySourcePermanentId();

        if (cardIndex == -1) {
            if (destination == GraveyardChoiceDestination.EXILE
                    || destination == GraveyardChoiceDestination.MAY_ABILITY_TARGET) {
                throw new IllegalStateException("Cannot decline forced graveyard choice");
            }
            // Player declined — if this is part of a "each player returns" flow, skip remaining
            // picks for this player by removing queued entries for the same player from the front.
            // Entries with skipRemainingOnDecline=false are independent choices (e.g. Grim Captain's
            // Call) and should not be removed when another choice is declined.
            UUID decliningPlayerId = playerId;
            while (!gameData.pendingGraveyardReturnQueue.isEmpty()
                    && gameData.pendingGraveyardReturnQueue.getFirst().playerId().equals(decliningPlayerId)
                    && gameData.pendingGraveyardReturnQueue.getFirst().skipRemainingOnDecline()) {
                gameData.pendingGraveyardReturnQueue.removeFirst();
            }
            String logEntry = player.getUsername() + " chooses not to return a card.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to return a card from graveyard", gameData.id, player.getUsername());
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            Card card;
            // Owner of the graveyard the card is leaving — used to return it if a continuous effect
            // (e.g. Grafdigger's Cage) stops a creature card from entering the battlefield.
            UUID cardGraveyardOwnerId = null;
            if (destination == GraveyardChoiceDestination.MAY_ABILITY_TARGET) {
                // MAY_ABILITY_TARGET: get reference without removal — the effect handler will exile it
                if (cardPool != null) {
                    card = cardPool.get(cardIndex);
                } else {
                    card = gameData.playerGraveyards.get(playerId).get(cardIndex);
                }
            } else if (cardPool != null) {
                // Cross-graveyard choice: card pool contains cards from any graveyard
                card = cardPool.get(cardIndex);
                cardGraveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
            } else {
                // Standard choice: indices into the player's own graveyard
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                card = graveyard.get(cardIndex);
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                cardGraveyardOwnerId = playerId;
            }

            switch (destination) {
                case HAND -> {
                    gameData.addCardToHand(playerId, card);

                    String logEntry = player.getUsername() + " returns " + card.getName() + " from graveyard to hand.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, player.getUsername(), card.getName());

                    if (gainLifeEqualToManaValue) {
                        int manaValue = card.getManaValue();
                        if (manaValue > 0) {
                            lifeSupport.applyGainLife(gameData, playerId, manaValue);
                        }
                    }
                }
                case BATTLEFIELD -> {
                    // Grafdigger's Cage etc.: a matching card (e.g. a creature card) can't enter the
                    // battlefield from a graveyard; it stays in the graveyard it was being returned from.
                    if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                        UUID returnTo = cardGraveyardOwnerId != null ? cardGraveyardOwnerId : playerId;
                        gameData.playerGraveyards.computeIfAbsent(returnTo, k -> new ArrayList<>()).add(card);
                        gameBroadcastService.logAndBroadcast(gameData, card.getName()
                                + " can't enter the battlefield from a graveyard; it stays in the graveyard.");
                        log.info("Game {} - {} blocked from entering the battlefield from a graveyard",
                                gameData.id, card.getName());
                        break;
                    }
                    Permanent perm = new Permanent(card);
                    if (grantColor != null) {
                        perm.getGrantedColors().add(grantColor);
                    }
                    if (grantSubtype != null && !perm.getGrantedSubtypes().contains(grantSubtype)) {
                        perm.getGrantedSubtypes().add(grantSubtype);
                    }
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, perm);

                    String logEntry = player.getUsername() + " puts " + card.getName() + " from a graveyard onto the battlefield.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} puts {} from graveyard onto battlefield", gameData.id, player.getUsername(), card.getName());

                    if (attachToSourcePermanentId != null) {
                        Permanent sourcePerm = gameQueryService.findPermanentById(gameData, attachToSourcePermanentId);
                        if (sourcePerm != null) {
                            gameData.interaction.setPendingEquipmentAttach(perm.getId(), sourcePerm.getId());
                            gameData.pendingMayAbilities.add(new PendingMayAbility(
                                    sourcePerm.getCard(), playerId, List.of(),
                                    sourcePerm.getCard().getName() + " — Attach " + card.getName()
                                            + " to " + sourcePerm.getCard().getName() + "?"
                            ));
                            playerInputService.processNextMayAbility(gameData);
                        }
                    }

                    if (card.hasType(CardType.CREATURE)) {
                        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);
                    }
                    if (!gameData.interaction.isAwaitingInput()) {
                        legendRuleService.checkLegendRule(gameData, playerId);
                    }
                }
                case EXILE -> {
                    if (trackWithSourcePermanentId != null) {
                        exileService.exileCard(gameData, playerId, card, trackWithSourcePermanentId);
                    } else {
                        exileService.exileCard(gameData, playerId, card);
                    }

                    String logEntry = player.getUsername() + " exiles " + card.getName() + " from their graveyard.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} exiles {} from graveyard", gameData.id, player.getUsername(), card.getName());

                    // Conditional life gain (e.g. Graveyard Shovel: "If it's a creature card, you gain 2 life.")
                    if (gainLifeIfCreatureAmount > 0 && gainLifeIfCreaturePlayerId != null
                            && card.hasType(CardType.CREATURE)) {
                        lifeSupport.applyGainLife(gameData, gainLifeIfCreaturePlayerId, gainLifeIfCreatureAmount);
                    }

                    // Check if more exiles are needed
                    int remaining = exileRemainingCount - 1;
                    if (remaining > 0) {
                        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                        if (graveyard != null && !graveyard.isEmpty()) {
                            List<Integer> newValidIndices = IntStream.range(0, graveyard.size()).boxed().toList();
                            interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                                    .builder(playerId, newValidIndices, GraveyardChoiceDestination.EXILE,
                                            "Choose a card to exile from your graveyard.")
                                    .exileRemainingCount(remaining)
                                    .build());
                            return;
                        }
                    }
                }
                case MAY_ABILITY_TARGET -> {
                    // Resolution-time flow: set target on pending entry and resume resolution
                    if (gameData.resolvedMayTargetingEntry != null) {
                        StackEntry pendingEntry = gameData.resolvedMayTargetingEntry;
                        gameData.resolvedMayTargetingEntry = null;
                        String resolveLog = player.getUsername() + " targets " + card.getName() + " in graveyard with "
                                + pendingEntry.getCard().getName() + "'s ability.";
                        gameBroadcastService.logAndBroadcast(gameData, resolveLog);
                        log.info("Game {} - {} targets {} in graveyard for may ability", gameData.id,
                                player.getUsername(), card.getName());
                        pendingEntry.setTargetId(card.getId());
                        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry, gameData.pendingEffectResolutionIndex);
                        if (!gameData.interaction.isAwaitingInput()) {
                            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                        }
                        return;
                    }

                    String logEntry = player.getUsername() + " targets " + card.getName() + " in graveyard with "
                            + mayAbilitySourceCard.getName() + "'s ability.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} targets {} in graveyard for may ability", gameData.id,
                            player.getUsername(), card.getName());

                    // Non-stack flow: create a new stack entry
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            mayAbilitySourceCard,
                            mayAbilityControllerId,
                            mayAbilitySourceCard.getName() + "'s ability",
                            new ArrayList<>(mayAbilityEffects),
                            card.getId(),
                            mayAbilitySourcePermanentId
                    );
                    gameData.stack.add(entry);
                }
            }
        }

        // Check if there are more "each player returns" graveyard choices queued
        if (!gameData.pendingGraveyardReturnQueue.isEmpty()) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleMultipleCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        if (gameData.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class) == null) {
            throw new IllegalStateException("Not awaiting multi-graveyard choice");
        }
        PendingInteraction.MultiGraveyardChoice multiGraveyardChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        if (multiGraveyardChoice == null || !player.getId().equals(multiGraveyardChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> validIds = multiGraveyardChoice.validCardIds();
        int maxCount = multiGraveyardChoice.maxCount();

        if (cardIds == null) {
            cardIds = List.of();
        }

        if (cardIds.size() > maxCount) {
            throw new IllegalStateException("Too many cards selected: " + cardIds.size() + " > " + maxCount);
        }

        // Spell targeting (e.g. Midnight Ritual) requires exactly X targets — "X target" is not "up to X target"
        // Exception: "any number of target" spells (e.g. Frantic Salvage) allow 0 to max
        StackEntryType pendingEntryTypeCheck = gameData.graveyardTargetOperation.entryType;
        int pendingXValueCheck = gameData.graveyardTargetOperation.xValue;
        boolean isAnyNumber = gameData.graveyardTargetOperation.anyNumber;
        if (pendingEntryTypeCheck != null && !isAnyNumber && cardIds.size() != pendingXValueCheck) {
            throw new IllegalStateException("Must choose exactly " + pendingXValueCheck + " targets, but chose " + cardIds.size());
        }

        Set<UUID> uniqueIds = new HashSet<>(cardIds);
        if (uniqueIds.size() != cardIds.size()) {
            throw new IllegalStateException("Duplicate card IDs in selection");
        }

        for (UUID cardId : cardIds) {
            if (!validIds.contains(cardId)) {
                throw new IllegalStateException("Invalid card: " + cardId);
            }
        }

        // Card pile separation (Boneyard Parley): opponent assigns exiled cards to piles
        PendingPileSeparation pileSeparation = gameData.peekPendingInteraction(PendingPileSeparation.class);
        if (pileSeparation != null && pileSeparation.cardPileMode()) {
            gameData.interaction.clearAwaitingInput();
            graveyardReturnSupport.completeCardPileSeparationStep1(gameData, cardIds);
            return;
        }

        // Retrieve the pending info
        Card pendingCard = gameData.graveyardTargetOperation.card;
        UUID controllerId = gameData.graveyardTargetOperation.controllerId;
        List<CardEffect> pendingEffects = gameData.graveyardTargetOperation.effects;
        StackEntryType pendingEntryType = gameData.graveyardTargetOperation.entryType;
        int pendingXValue = gameData.graveyardTargetOperation.xValue;
        UUID pendingTargetPlayerId = gameData.graveyardTargetOperation.targetPlayerId;
        boolean pendingFlashback = gameData.graveyardTargetOperation.flashback;
        UUID pendingSourcePermanentId = gameData.graveyardTargetOperation.sourcePermanentId;
        String pendingChapterName = gameData.graveyardTargetOperation.chapterName;

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();
        gameData.graveyardTargetOperation.card = null;
        gameData.graveyardTargetOperation.controllerId = null;
        gameData.graveyardTargetOperation.effects = null;
        gameData.graveyardTargetOperation.entryType = null;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = false;
        gameData.graveyardTargetOperation.targetPlayerId = null;
        gameData.graveyardTargetOperation.flashback = false;
        gameData.graveyardTargetOperation.sourcePermanentId = null;
        gameData.graveyardTargetOperation.chapterName = null;

        List<String> targetNames = new ArrayList<>();
        for (UUID cardId : cardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                targetNames.add(card.getName());
            }
        }

        if (pendingEntryType != null) {
            // Spell casting — put spell on stack with targets
            StackEntry spellEntry = new StackEntry(
                    pendingEntryType, pendingCard, controllerId, pendingCard.getName(),
                    new ArrayList<>(pendingEffects), pendingXValue, pendingTargetPlayerId,
                    null, Map.of(), null, new ArrayList<>(cardIds), List.of()
            );
            if (pendingFlashback) {
                spellEntry.setCastWithFlashback(true);
            }
            spellEntry.setSourceZone(pendingFlashback ? Zone.GRAVEYARD : Zone.HAND);
            gameData.stack.add(spellEntry);

            gameData.recordSpellCast(controllerId, pendingCard);
            gameData.priorityPassedBy.clear();

            String castLog = gameData.playerIdToName.get(controllerId) + " casts " + pendingCard.getName()
                    + " targeting " + String.join(", ", targetNames) + ".";
            gameBroadcastService.logAndBroadcast(gameData, castLog);
            log.info("Game {} - {} casts {} with {} graveyard targets", gameData.id, pendingCard.getName(),
                    pendingCard.getName(), cardIds.size());

            triggerCollectionService.checkSpellCastTriggers(gameData, pendingCard, controllerId,
                    !pendingFlashback);
            gameBroadcastService.broadcastGameState(gameData);
        } else {
            // Triggered ability (ETB, spell-cast trigger, or saga chapter) — put on stack with targets
            String description;
            if (pendingChapterName != null) {
                description = pendingCard.getName() + "'s chapter " + pendingChapterName + " ability";
            } else {
                description = pendingCard.getName() + "'s ability";
            }

            StackEntry triggeredEntry;
            if (pendingSourcePermanentId != null) {
                // Saga chapter: include sourcePermanentId for SBA check (CR 714.4)
                triggeredEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pendingCard,
                        controllerId,
                        description,
                        new ArrayList<>(pendingEffects),
                        0,
                        null,
                        pendingSourcePermanentId,
                        Map.of(),
                        null,
                        new ArrayList<>(cardIds),
                        List.of()
                );
            } else {
                triggeredEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        pendingCard,
                        controllerId,
                        description,
                        new ArrayList<>(pendingEffects),
                        new ArrayList<>(cardIds)
                );
            }
            gameData.stack.add(triggeredEntry);

            if (cardIds.isEmpty()) {
                String triggerLog = description + " triggers targeting no cards.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
            } else {
                String triggerLog = description + " triggers targeting " + String.join(", ", targetNames) + ".";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
            }
            log.info("Game {} - {} triggered ability pushed onto stack with {} graveyard targets", gameData.id, pendingCard.getName(), cardIds.size());
        }

        // Process any remaining pending saga chapter graveyard targets before auto-pass
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SagaChapterGraveyardTarget.class)) {
            triggerCollectionService.processNextSagaChapterGraveyardTarget(gameData);
            return;
        }

        // Process any remaining pending graveyard-target triggers before auto-pass
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }
}


