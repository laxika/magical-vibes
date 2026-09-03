package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.PendingTruthOrTaleCardChoice;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.IndependentlyTargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileDragonApproachAndSearchSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.CollectEvidenceEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LandCopyOnEnterService;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerMayExileGraveyardCardsSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SpellweaverVoluteSupport;
import com.github.laxika.magicalvibes.service.effect.DredgeSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraveyardChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CloneService cloneService;
    private final LegendRuleService legendRuleService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final LifeSupport lifeSupport;
    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport brilliantUltimatumSupport;
    private final InputCompletionService inputCompletionService;
    private final com.github.laxika.magicalvibes.service.effect.EffectResolutionService effectResolutionService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final ETBTokenTargetService etbTokenTargetService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.ExileMatchingCardsFromGraveyardAndLibrarySupport
            exileMatchingCardsSupport;
    private final DestructionSupport destructionSupport;
    private final ExileDragonApproachAndSearchSupport exileDragonApproachAndSearchSupport;
    private final CollectEvidenceEffectHandler collectEvidenceEffectHandler;
    private final LandCopyOnEnterService landCopyOnEnterService;
    private final StateBasedActionService stateBasedActionService;
    private final EachPlayerMayExileGraveyardCardsSupport eachPlayerMayExileGraveyardCardsSupport;
    private final DredgeSupport dredgeSupport;
    private final SpellweaverVoluteSupport spellweaverVoluteSupport;

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

        GraveyardChoiceDestination destination = graveyardChoice.destination();

        // Validate before touching interaction state: a rejected answer must leave the prompt
        // standing so the player can answer again. Clearing first and then throwing (both checks
        // are repeated below as defence) destroys the only thing that would resume the entry
        // parked in pendingEffectResolutionEntry, wedging the game on a stale client answer.
        if (cardIndex == -1) {
            if (destination == GraveyardChoiceDestination.EXILE
                    || destination == GraveyardChoiceDestination.MAY_ABILITY_TARGET
                    || destination == GraveyardChoiceDestination.COPY_ON_ENTER
                    || graveyardChoice.mandatory()) {
                throw new IllegalStateException("Cannot decline forced graveyard choice");
            }
        } else if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        if (destination == GraveyardChoiceDestination.DREDGE) {
            gameData.interaction.clearAwaitingInput();
            dredgeSupport.handleChoice(gameData, player, cardIndex);
            return;
        }

        gameData.interaction.clearAwaitingInput();

        if (gameData.queenKaylaBinKroogOperation.awaitingChoice) {
            gameData.queenKaylaBinKroogOperation.awaitingChoice = false;
            gameData.queenKaylaBinKroogOperation.choiceMade = true;
            gameData.queenKaylaBinKroogOperation.chosenCardId = cardIndex == -1
                    ? null : cardPool.get(cardIndex).getId();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Forgotten Lore: the opponent only *names* a card in the controller's graveyard — nothing
        // moves yet. Record it and resume the paused resolution so the handler can offer the {G}.
        if (gameData.graveyardTargetOperation.resolutionTimeForgottenLoreResume) {
            gameData.graveyardTargetOperation.resolutionTimeForgottenLoreResume = false;
            Card chosen = cardPool.get(cardIndex);
            gameData.forgottenLore.pendingChosenCardId = chosen.getId();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses ", chosen, " from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Phyrexian Grimoire: the opponent only picks which of the two cards is exiled — nothing
        // moves yet. Record it and resume the paused resolution so the handler can move both cards.
        if (gameData.graveyardTargetOperation.resolutionTimePhyrexianGrimoireResume) {
            gameData.graveyardTargetOperation.resolutionTimePhyrexianGrimoireResume = false;
            Card chosen = cardPool.get(cardIndex);
            gameData.graveyardTargetOperation.phyrexianGrimoireChosenCardId = chosen.getId();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses ", chosen, " from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeWakeToSlaughterResume) {
            gameData.graveyardTargetOperation.resolutionTimeWakeToSlaughterResume = false;
            Card chosen = cardPool.get(cardIndex);
            gameData.graveyardTargetOperation.wakeToSlaughterChosenCardId = chosen.getId();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses ", chosen, " from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeScroungeResume) {
            gameData.graveyardTargetOperation.resolutionTimeScroungeResume = false;
            Card chosen = cardPool.get(cardIndex);
            gameData.graveyardTargetOperation.scroungeChosenCardId = chosen.getId();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses ", chosen, " from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeOpponentChoosesCardToHandResume) {
            gameData.graveyardTargetOperation.resolutionTimeOpponentChoosesCardToHandResume = false;
            Card chosen = cardPool.get(cardIndex);
            gameData.graveyardTargetOperation.opponentChoosesCardToHandChosenCardId = chosen.getId();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses ", chosen, " from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeOpponentChoosesCardToHandResume) {
            gameData.graveyardTargetOperation.resolutionTimeOpponentChoosesCardToHandResume = false;
            Card chosen = cardPool.get(cardIndex);
            gameData.graveyardTargetOperation.opponentChoosesCardToHandChosenCardId = chosen.getId();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses ", chosen, " from the graveyard."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        boolean gainLifeEqualToManaValue = graveyardChoice.gainLifeEqualToManaValue();
        UUID attachToSourcePermanentId = graveyardChoice.attachToSourcePermanentId();
        CardColor grantColor = graveyardChoice.grantColor();
        CardSubtype grantSubtype = graveyardChoice.grantSubtype();
        int exileRemainingCount = graveyardChoice.exileRemainingCount();
        int gainLifeIfCreatureAmount = graveyardChoice.gainLifeIfCreatureAmount();
        UUID gainLifeIfCreaturePlayerId = graveyardChoice.gainLifeIfCreaturePlayerId();
        UUID trackWithSourcePermanentId = graveyardChoice.trackWithSourcePermanentId();
        CardSubtype grantSourceHasteIfSubtype = graveyardChoice.grantSourceHasteIfSubtype();
        UUID grantSourceHasteSourcePermanentId = graveyardChoice.grantSourceHasteSourcePermanentId();
        boolean enterTapped = graveyardChoice.enterTapped();
        boolean exileIfLeavesBattlefield = graveyardChoice.exileIfLeavesBattlefield();
        UUID destinationControllerId = graveyardChoice.destinationControllerId();
        // May ability graveyard targeting context
        Card mayAbilitySourceCard = graveyardChoice.mayAbilitySourceCard();
        UUID mayAbilityControllerId = graveyardChoice.mayAbilityControllerId();
        java.util.List<CardEffect> mayAbilityEffects = graveyardChoice.mayAbilityEffects();
        UUID mayAbilitySourcePermanentId = graveyardChoice.mayAbilitySourcePermanentId();

        if (cardIndex == -1) {
            if (destination == GraveyardChoiceDestination.EXILE
                    || destination == GraveyardChoiceDestination.MAY_ABILITY_TARGET
                    || destination == GraveyardChoiceDestination.COPY_ON_ENTER
                    || graveyardChoice.mandatory()) {
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
            gameLogService.append(gameData, GameLog.text(logEntry));
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
            } else if (destination == GraveyardChoiceDestination.COPY_ON_ENTER) {
                card = cardPool.get(cardIndex);
            } else if (cardPool != null) {
                // Cross-graveyard choice: card pool contains cards from any graveyard
                card = cardPool.get(cardIndex);
                cardGraveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
                if (destination == GraveyardChoiceDestination.EXILE) {
                    permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
                } else {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                }
            } else {
                // Standard choice: indices into the player's own graveyard
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                card = graveyard.get(cardIndex);
                if (destination == GraveyardChoiceDestination.EXILE) {
                    permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
                } else {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                }
                cardGraveyardOwnerId = playerId;
            }

            switch (destination) {
                case HAND -> {
                    permanentRemovalService.addCardToHandFromGraveyard(
                            gameData, cardGraveyardOwnerId, playerId, card);

                    gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " returns " , card, " from graveyard to hand."));
                    log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, player.getUsername(), card.getName());

                    if (gainLifeEqualToManaValue) {
                        int manaValue = card.getManaValue();
                        if (manaValue > 0) {
                            lifeSupport.applyGainLife(gameData, playerId, manaValue);
                        }
                    }

                    // Warren Pilferers: "If that card is a Goblin card, this creature gains haste
                    // until end of turn." The subtype is checked against the returned card (changeling-aware).
                    if (grantSourceHasteIfSubtype != null && grantSourceHasteSourcePermanentId != null
                            && (gameQueryService.cardHasSubtype(card, grantSourceHasteIfSubtype, gameData, playerId)
                                || card.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.CHANGELING))) {
                        Permanent sourcePerm = gameQueryService.findPermanentById(gameData, grantSourceHasteSourcePermanentId);
                        if (sourcePerm != null) {
                            sourcePerm.getGrantedKeywords().add(com.github.laxika.magicalvibes.model.Keyword.HASTE);
                            gameLogService.append(gameData, GameLog.cardThen(sourcePerm.getCard(), " gains haste until end of turn."));
                        }
                    }

                    if (!gameData.pendingGraveyardReturnQueue.isEmpty()
                            && gameData.pendingGraveyardReturnQueue.getFirst().distinctManaValues()) {
                        PendingGraveyardReturnChoice next = gameData.pendingGraveyardReturnQueue.removeFirst();
                        Set<Integer> excludedManaValues = new HashSet<>(next.excludedManaValues());
                        excludedManaValues.add(card.getManaValue());
                        gameData.pendingGraveyardReturnQueue.addFirst(new PendingGraveyardReturnChoice(
                                next.playerId(), next.remainingCount(), next.filter(), next.destination(),
                                next.skipRemainingOnDecline(), next.mandatory(), next.fromBattlefieldThisTurn(),
                                next.distinctManaValues(), excludedManaValues));
                    }
                }
                case BATTLEFIELD -> {
                    if (gameData.pendingGraveyardReturnBatch != null) {
                        gameData.pendingGraveyardReturnBatch = gameData.pendingGraveyardReturnBatch.add(
                                card, cardGraveyardOwnerId);
                        gameLogService.append(gameData, GameLog.textCardText(
                                player.getUsername() + " chooses ", card, " from a graveyard."));
                        break;
                    }
                    // Grafdigger's Cage etc.: a matching card (e.g. a creature card) can't enter the
                    // battlefield from a graveyard; it stays in the graveyard it was being returned from.
                    if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                        UUID returnTo = cardGraveyardOwnerId != null ? cardGraveyardOwnerId : playerId;
                        gameData.playerGraveyards.computeIfAbsent(returnTo, k -> new ArrayList<>()).add(card);
                        gameLogService.append(gameData, GameLog.cardThen(card, " can't enter the battlefield from a graveyard; it stays in the graveyard."));
                        log.info("Game {} - {} blocked from entering the battlefield from a graveyard",
                                gameData.id, card.getName());
                        break;
                    }
                    Permanent perm = new Permanent(card);
                    perm.setEnteredFromGraveyardOwnerId(cardGraveyardOwnerId);
                    perm.setExileIfLeavesBattlefield(exileIfLeavesBattlefield);
                    if (card.hasType(CardType.PLANESWALKER)) {
                        int startingLoyalty = card.getLoyalty() != null ? card.getLoyalty() : 0;
                        startingLoyalty = gameQueryService.replaceCounters(gameData, perm, playerId,
                                CounterType.LOYALTY, startingLoyalty, playerId);
                        perm.setCounterCount(CounterType.LOYALTY, startingLoyalty);
                    }
                    if (grantColor != null) {
                        perm.getGrantedColors().add(grantColor);
                    }
                    if (grantSubtype != null && !perm.getGrantedSubtypes().contains(grantSubtype)) {
                        perm.getGrantedSubtypes().add(grantSubtype);
                    }
                    if (enterTapped) {
                        perm.tap();
                    }
                    UUID battlefieldControllerId = destinationControllerId != null
                            ? destinationControllerId : playerId;
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, battlefieldControllerId, perm);
                    if (cardGraveyardOwnerId != null && !cardGraveyardOwnerId.equals(battlefieldControllerId)) {
                        graveyardReturnSupport.trackStolenCreature(
                                gameData, perm.getId(), battlefieldControllerId, cardGraveyardOwnerId);
                    }

                    if (graveyardChoice.enterWithCounter() != null
                            && graveyardChoice.enterWithCounterCount() > 0) {
                        permanentCounterSupport.placeCounterOnPermanent(
                                gameData, gameData.pendingEffectResolutionEntry, perm,
                                graveyardChoice.enterWithCounter(), graveyardChoice.enterWithCounterCount());
                    }
                    if (graveyardChoice.enterWithCounters() != null) {
                        for (CounterType counterType : graveyardChoice.enterWithCounters()) {
                            permanentCounterSupport.placeCounterOnPermanent(
                                    gameData, gameData.pendingEffectResolutionEntry, perm, counterType, 1);
                        }
                    }

                    gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " puts " , card, " from a graveyard onto the battlefield."));
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
                        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, battlefieldControllerId, card, null, false);
                    }
                    if (!gameData.interaction.isAwaitingInput()) {
                        legendRuleService.checkLegendRule(gameData, battlefieldControllerId);
                    }
                }
                case COPY_ON_ENTER -> {
                    landCopyOnEnterService.complete(gameData, card);
                    if (!gameData.interaction.isAwaitingInput()) {
                        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
                    }
                    return;
                }
                case SHUFFLE_INTO_OWNERS_LIBRARY -> {
                    // Card already removed from the graveyard above; shuffle it into the owner's library.
                    gameData.playerDecks.get(playerId).add(card);
                    com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper
                            .shuffleLibrary(gameData, playerId);

                    
                    gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " shuffles ").card(card).text(" from their graveyard into their library.").build());
                    log.info("Game {} - {} shuffles {} from graveyard into library", gameData.id,
                            player.getUsername(), card.getName());
                }
                case TOP_OF_OWNERS_LIBRARY -> {
                    UUID libraryOwnerId = cardGraveyardOwnerId != null ? cardGraveyardOwnerId : playerId;
                    gameData.playerDecks.get(libraryOwnerId).addFirst(card);

                    
                    gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " puts ").card(card).text(" on top of their library from their graveyard.").build());
                    log.info("Game {} - {} puts {} on top of library from graveyard", gameData.id,
                            player.getUsername(), card.getName());
                }
                case BOTTOM_OF_OWNERS_LIBRARY -> {
                    UUID libraryOwnerId = cardGraveyardOwnerId != null ? cardGraveyardOwnerId : playerId;
                    gameData.playerDecks.get(libraryOwnerId).addLast(card);

                    
                    gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " puts ").card(card).text(" on the bottom of their library from their graveyard.").build());
                    log.info("Game {} - {} puts {} on bottom of library from graveyard", gameData.id,
                            player.getUsername(), card.getName());
                }
                case EXILE -> {
                    if (trackWithSourcePermanentId != null) {
                        exileService.exileCard(gameData, playerId, card, trackWithSourcePermanentId);
                    } else {
                        exileService.exileCard(gameData, playerId, card);
                    }

                    gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " exiles " , card, " from their graveyard."));
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
                        
                        gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " targets ").card(card).text(" in graveyard with ").card(pendingEntry.getCard()).text("'s ability.").build());
                        log.info("Game {} - {} targets {} in graveyard for may ability", gameData.id,
                                player.getUsername(), card.getName());
                        pendingEntry.setTargetId(card.getId());
                        triggerCollectionService.checkTargetChoiceTriggers(gameData, pendingEntry);
                        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry, gameData.pendingEffectResolutionIndex);
                        if (!gameData.interaction.isAwaitingInput()) {
                            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                        }
                        return;
                    }

                    
                    gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " targets ").card(card).text(" in graveyard with ").card(mayAbilitySourceCard).text("'s ability.").build());
                    log.info("Game {} - {} targets {} in graveyard for may ability", gameData.id,
                            player.getUsername(), card.getName());

                    // Non-stack flow: create a new stack entry. The chosen target is a card in a
                    // graveyard, so the entry declares that zone — otherwise an effect that really
                    // does target (Isareth the Awakener's reanimation trigger) would be checked
                    // against the battlefield on resolution and fizzle.
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            mayAbilitySourceCard,
                            mayAbilityControllerId,
                            mayAbilitySourceCard.getName() + "'s ability",
                            new ArrayList<>(mayAbilityEffects),
                            0,
                            card.getId(),
                            mayAbilitySourcePermanentId,
                            null,
                            Zone.GRAVEYARD,
                            null,
                            null
                    );
                    gameData.stack.add(entry);
                    triggerCollectionService.checkTargetChoiceTriggers(gameData, entry);
                }
            }
        }

        // Check if there are more "each player returns" graveyard choices queued
        if (!gameData.pendingGraveyardReturnQueue.isEmpty()) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        } else if (gameData.pendingGraveyardReturnBatch != null) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        // Resume the paused spell/ability resolution that began this choice, so effects after
        // the graveyard-return effect run now. Left dangling, the resumption state would fire
        // spuriously from a later, unrelated interaction completion (e.g. Beacon of Unrest
        // getting shuffled into the library a second time).
        if (gameData.pendingEffectResolutionEntry != null && !gameData.interaction.isAwaitingInput()) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
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

        if (gameData.graveyardTargetOperation
                .resolutionTimeShuffleUpToThreeCardsFromEachGraveyardResume) {
            Map<UUID, Integer> selectedPerOwner = new java.util.HashMap<>();
            for (UUID cardId : cardIds) {
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                if (ownerId == null || selectedPerOwner.merge(ownerId, 1, Integer::sum) > 3) {
                    throw new IllegalStateException("Choose at most three cards from each graveyard");
                }
            }
        }

        if (cardIds.size() > maxCount) {
            throw new IllegalStateException("Too many cards selected: " + cardIds.size() + " > " + maxCount);
        }

        // Spell targeting (e.g. Midnight Ritual) requires exactly X targets — "X target" is not "up to X target"
        // Exception: "any number of target" spells (e.g. Frantic Salvage) allow 0 to max
        StackEntryType pendingEntryTypeCheck = gameData.graveyardTargetOperation.entryType;
        int pendingXValueCheck = gameData.graveyardTargetOperation.xValue;
        boolean isAnyNumber = gameData.graveyardTargetOperation.anyNumber;
        if (pendingEntryTypeCheck != null && !isAnyNumber
                && multiGraveyardChoice.minCount() > 0
                && cardIds.size() != pendingXValueCheck) {
            throw new IllegalStateException("Must choose exactly " + pendingXValueCheck + " targets, but chose " + cardIds.size());
        }

        // A mandatory choice (Gifts Ungiven's "chooses two") must be answered in full; rejecting the
        // answer before any state is touched leaves the prompt standing so it can be answered again.
        if (cardIds.size() < multiGraveyardChoice.minCount()) {
            throw new IllegalStateException("Must choose " + multiGraveyardChoice.minCount()
                    + " cards, but chose " + cardIds.size());
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

        if (gameData.graveyardTargetOperation.resolutionTimeKayaSpiritsJusticeResume) {
            gameData.graveyardTargetOperation.resolutionTimeKayaSpiritsJusticeResume = false;
            gameData.interaction.clearAwaitingInput();

            if (cardIds.isEmpty()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            UUID chosenCardId = cardIds.getFirst();
            Card chosenCard = multiGraveyardChoice.cards().stream()
                    .filter(card -> card.getId().equals(chosenCardId))
                    .findFirst()
                    .orElse(null);
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (chosenCard == null || gameData.findExiledCard(chosenCard.getId()) == null
                    || pendingEntry == null) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            UUID controllerId = pendingEntry.getControllerId();
            List<UUID> tokenIds = gameData.playerBattlefields
                    .getOrDefault(controllerId, List.of()).stream()
                    .filter(permanent -> permanent.getCard().isToken())
                    .map(Permanent::getId)
                    .toList();
            if (tokenIds.isEmpty()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            playerInputService.beginPermanentChoice(gameData, controllerId, tokenIds,
                    new PermanentChoiceContext.KayaSpiritsJusticeTokenChoice(
                            pendingEntry.getCard(), controllerId, chosenCard),
                    "Choose a token you control to become a copy of the chosen creature card.");
            return;
        }

        int selectedManaValue = cardIds.stream()
                .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                .filter(java.util.Objects::nonNull)
                .mapToInt(Card::getManaValue)
                .sum();
        if (multiGraveyardChoice.minimumTotalManaValue() != null
                && selectedManaValue < multiGraveyardChoice.minimumTotalManaValue()) {
            throw new IllegalStateException("Selected cards do not have enough total mana value");
        }
        if (multiGraveyardChoice.maxTotalManaValue() != null
                && selectedManaValue > multiGraveyardChoice.maxTotalManaValue()) {
            throw new IllegalStateException("Selected cards exceed the total mana value "
                    + multiGraveyardChoice.maxTotalManaValue() + " limit");
        }

        var deadlyCoverUpContext = gameData.graveyardTargetOperation.resolutionTimeDeadlyCoverUp;
        if (deadlyCoverUpContext != null && deadlyCoverUpContext.chosenCardId() == null) {
            gameData.graveyardTargetOperation.resolutionTimeDeadlyCoverUp =
                    new com.github.laxika.magicalvibes.model.GraveyardTargetOperationState.DeadlyCoverUpContext(
                            cardIds.getFirst());
            gameData.interaction.clearAwaitingInput();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeCollectEvidenceResume) {
            gameData.graveyardTargetOperation.resolutionTimeCollectEvidenceResume = false;
            gameData.rerunCurrentEffectAfterInteraction = false;
            gameData.interaction.clearAwaitingInput();

            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            CollectEvidenceEffect collectEvidence = null;
            if (pendingEntry != null
                    && gameData.pendingEffectResolutionIndex < pendingEntry.getEffectsToResolve().size()
                    && pendingEntry.getEffectsToResolve().get(gameData.pendingEffectResolutionIndex)
                    instanceof CollectEvidenceEffect effect) {
                collectEvidence = effect;
            }

            List<Card> exiledCards = cardIds.stream()
                    .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (gameData.pendingEffectResolutionEntry != null) {
                gameData.pendingEffectResolutionEntry.setEventValue(
                        exiledCards.stream().mapToInt(Card::getManaValue).sum());
            }
            List<Card> graveyard = gameData.playerGraveyards.get(player.getId());
            if (graveyard != null) {
                graveyard.removeAll(exiledCards);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, player.getId(), exiledCards);
                for (Card card : exiledCards) {
                    exileService.exileCard(gameData, player.getId(), card);
                }
            }
            if (graveyard != null) {
                gameLogService.append(gameData, GameLog.text(player.getUsername() + " exiles "
                        + exiledCards.size() + " cards from their graveyard to collect evidence."));
            }
            triggerCollectionService.checkCollectEvidenceTriggers(gameData, player.getId());
            if (collectEvidence != null && pendingEntry != null) {
                collectEvidenceEffectHandler.queueReflexiveAbility(
                        gameData, pendingEntry, collectEvidence.thenEffect());
            }
            gameData.pendingEffectResolutionIndex++;
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.pendingSpellweaverVoluteReattachment != null
                && gameData.pendingSpellweaverVoluteReattachment.copyCardId() == null) {
            gameData.interaction.clearAwaitingInput();
            spellweaverVoluteSupport.completeAttachmentChoice(gameData, cardIds.getFirst());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeExileAnyNumberThenEffectResume) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.resolutionTimeExileAnyNumberThenEffectResume = false;
            gameData.graveyardTargetOperation.resolutionTimeExileAnyNumberThenEffectChoiceMade = true;
            gameData.graveyardTargetOperation.resolutionTimeExileAnyNumberThenEffectChosenCardIds =
                    List.copyOf(cardIds);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.cloneOperation.copyCardFilter != null) {
            gameData.interaction.clearAwaitingInput();
            gameData.interaction.clearPermanentChoiceContext();
            cloneService.completeCloneEntryFromGraveyard(gameData, cardIds.getFirst());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeExileNCardsThenEffectResume) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.resolutionTimeExileNCardsThenEffectChosenCardIds =
                    List.copyOf(cardIds);
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.milledCreatureReturn != null) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.milledCreatureReturn =
                    new GraveyardTargetOperationState.MilledCreatureReturnContext(List.copyOf(cardIds));
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.milledCreaturesToHand != null) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.milledCreaturesToHand =
                    new GraveyardTargetOperationState.MilledCreaturesToHandContext(List.copyOf(cardIds));
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.cloneOperation.graveyardCopyChoicePending) {
            Card selectedCard = gameQueryService.findCardInGraveyardById(gameData, cardIds.getFirst());
            if (selectedCard == null || !selectedCard.hasType(CardType.CREATURE)) {
                throw new IllegalStateException("Chosen creature card is no longer in a graveyard");
            }

            gameData.interaction.clearAwaitingInput();
            cloneService.completeCloneEntryFromGraveyardChoice(gameData, selectedCard);
            stateBasedActionService.performStateBasedActions(gameData);

            if (gameData.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)) {
                triggerCollectionService.processNextDeathTriggerTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            if (gameData.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)) {
                triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }

            if (!gameData.pendingMayAbilities.isEmpty()) {
                playerInputService.processNextMayAbility(gameData);
                return;
            }
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.eachPlayerMayExileGraveyardCards.active
                && player.getId().equals(gameData.eachPlayerMayExileGraveyardCards.currentPlayerId)) {
            gameData.interaction.clearAwaitingInput();
            eachPlayerMayExileGraveyardCardsSupport.completeSelection(
                    gameData, player.getId(), cardIds);
            gameData.eachPlayerMayExileGraveyardCards.currentPlayerId = null;
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeReturnCardsToBattlefieldResume) {
            gameData.graveyardTargetOperation.resolutionTimeReturnCardsToBattlefieldResume = false;
            gameData.interaction.clearAwaitingInput();
            for (UUID cardId : cardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
                    graveyardReturnSupport.putCardOntoBattlefield(gameData, player.getId(), card);
                }
            }
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeBargainedReturnChoiceResume) {
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (pendingEntry == null) {
                throw new IllegalStateException("Missing paused bargained graveyard return resolution");
            }
            gameData.graveyardTargetOperation.resolutionTimeBargainedReturnChoiceResume = false;
            List<UUID> targetCardIds = gameData.graveyardTargetOperation
                    .resolutionTimeBargainedReturnTargetCardIds;
            gameData.graveyardTargetOperation.resolutionTimeBargainedReturnTargetCardIds = List.of();
            gameData.interaction.clearAwaitingInput();
            Set<UUID> battlefieldCardIds = new HashSet<>(cardIds);
            graveyardReturnSupport.processTargetedGraveyardTargets(
                    gameData, pendingEntry, targetCardIds,
                    (graveyard, card) -> {
                        if (battlefieldCardIds.contains(card.getId())) {
                            graveyardReturnSupport.putCardOntoBattlefield(
                                    gameData, player.getId(), card);
                        } else {
                            permanentRemovalService.addCardToHandFromGraveyard(
                                    gameData, player.getId(), player.getId(), card);
                        }
                    },
                    " returns ", " from graveyard.");
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeExileThenMayBecomeCopyResume) {
            gameData.graveyardTargetOperation.resolutionTimeExileThenMayBecomeCopyResume = false;
            gameData.interaction.clearAwaitingInput();
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (!cardIds.isEmpty() && pendingEntry != null) {
                UUID cardId = cardIds.getFirst();
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card)
                        && card.hasType(CardType.CREATURE)) {
                    gameData.pendingMayAbilities.add(new PendingMayAbility(
                            pendingEntry.getCard(), pendingEntry.getControllerId(),
                            List.of(new BecomeCopyOfCardUntilEndOfTurnEffect(card)),
                            "Have " + pendingEntry.getCard().getName() + " become a copy of "
                                    + card.getName() + " until end of turn?",
                            null, null, pendingEntry.getSourcePermanentId()));
                }
            }
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsResume) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsResume = false;
            gameData.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsChoiceMade = true;
            gameData.graveyardTargetOperation.resolutionTimePutOnBottomThenExileTopCardsChosenCardId =
                    cardIds.isEmpty() ? null : cardIds.getFirst();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersResume) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersResume = false;
            gameData.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersChoiceMade = true;
            gameData.graveyardTargetOperation.resolutionTimeExileOwnGraveyardCardPutCountersChosenCardId =
                    cardIds.isEmpty() ? null : cardIds.getFirst();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeDragonApproachResume) {
            StackEntry entry = gameData.pendingEffectResolutionEntry;
            if (entry == null) {
                throw new IllegalStateException("Missing paused Dragon's Approach resolution");
            }
            gameData.interaction.clearAwaitingInput();
            exileDragonApproachAndSearchSupport.complete(gameData, entry, cardIds);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        var exileFromEachContext = gameData.graveyardTargetOperation
                .resolutionTimeExileUpToOneMatchingCardFromEachGraveyardResume;
        if (exileFromEachContext != null) {
            Set<UUID> selectedOwners = new HashSet<>();
            for (UUID cardId : cardIds) {
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                if (ownerId == null || !selectedOwners.add(ownerId)) {
                    throw new IllegalStateException("Choose at most one card from each graveyard");
                }
            }

            gameData.graveyardTargetOperation
                    .resolutionTimeExileUpToOneMatchingCardFromEachGraveyardResume = null;
            gameData.interaction.clearAwaitingInput();
            for (UUID cardId : cardIds) {
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (ownerId != null && card != null) {
                    permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
                    exileService.exileCard(gameData, ownerId, card, exileFromEachContext.sourcePermanentId());
                }
            }
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation
                .resolutionTimeShuffleUpToThreeCardsFromEachGraveyardResume) {
            Map<UUID, Integer> selectedPerOwner = new java.util.HashMap<>();
            for (UUID cardId : cardIds) {
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                if (ownerId == null || selectedPerOwner.merge(ownerId, 1, Integer::sum) > 3) {
                    throw new IllegalStateException("Choose at most three cards from each graveyard");
                }
            }

            gameData.graveyardTargetOperation
                    .resolutionTimeShuffleUpToThreeCardsFromEachGraveyardResume = false;
            gameData.interaction.clearAwaitingInput();
            Set<UUID> shuffledOwners = new HashSet<>();
            for (UUID cardId : cardIds) {
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (ownerId != null && card != null) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
                    gameData.playerDecks.get(ownerId).add(card);
                    shuffledOwners.add(ownerId);
                }
            }
            for (UUID ownerId : shuffledOwners) {
                Collections.shuffle(gameData.playerDecks.get(ownerId), ThreadLocalRandom.current());
            }
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation
                .resolutionTimeExileThenPutCountersOnSharedTypeCreaturesResume) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation
                    .resolutionTimeExileThenPutCountersOnSharedTypeCreaturesResume = false;
            gameData.graveyardTargetOperation
                    .resolutionTimeExileThenPutCountersOnSharedTypeCreaturesChoiceMade = true;
            gameData.graveyardTargetOperation
                    .resolutionTimeExileThenPutCountersOnSharedTypeCreaturesChosenCardId =
                    cardIds.isEmpty() ? null : cardIds.getFirst();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (multiGraveyardChoice.reorderToLibraryTop()) {
            gameData.interaction.clearAwaitingInput();
            List<Card> selectedCards = new ArrayList<>();
            for (UUID cardId : cardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
                    selectedCards.add(card);
                }
            }

            if (selectedCards.size() > 1) {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                        player.getId(), selectedCards, false, player.getId(),
                        "Put these cards on top of your library in any order (top to bottom)."));
            } else {
                if (selectedCards.size() == 1) {
                    gameData.playerDecks.get(player.getId()).addFirst(selectedCards.getFirst());
                }
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        List<CardEffect> pendingEffects = gameData.graveyardTargetOperation.effects;
        BattlefieldAndGraveyardCardChoosingEffect mixedZoneEffect = pendingEffects == null ? null
                : pendingEffects.stream()
                .filter(BattlefieldAndGraveyardCardChoosingEffect.class::isInstance)
                .map(BattlefieldAndGraveyardCardChoosingEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (mixedZoneEffect != null) {
            validateMixedZoneSelection(gameData, cardIds, mixedZoneEffect);
        }
        CardEffect activeSpellGraveyardChoiceEffect =
                gameData.graveyardTargetOperation.activeSpellGraveyardChoiceEffect;
        List<CardEffect> targetValidationEffects = activeSpellGraveyardChoiceEffect != null
                ? List.of(activeSpellGraveyardChoiceEffect) : pendingEffects;
        ReturnUpToOneOfEachFilterFromGraveyardToHandEffect oneOfEachFilterEffect = targetValidationEffects == null
                ? null
                : targetValidationEffects.stream()
                .filter(ReturnUpToOneOfEachFilterFromGraveyardToHandEffect.class::isInstance)
                .map(ReturnUpToOneOfEachFilterFromGraveyardToHandEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (oneOfEachFilterEffect != null
                && !canAssignEachFilter(gameData, cardIds, oneOfEachFilterEffect)) {
            throw new IllegalStateException("Each selected card must match a different target group");
        }

        ReturnTargetCardsFromGraveyardToHandEffect sharedCreatureTypeEffect = targetValidationEffects == null
                ? null
                : targetValidationEffects.stream()
                .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                .map(ReturnTargetCardsFromGraveyardToHandEffect.class::cast)
                .filter(ReturnTargetCardsFromGraveyardToHandEffect::requireSharedCreatureType)
                .findFirst()
                .orElse(null);
        if (sharedCreatureTypeEffect != null && cardIds.size() == sharedCreatureTypeEffect.minTargets()) {
            List<Card> selectedCards = cardIds.stream()
                    .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (selectedCards.size() == 2
                    && !gameQueryService.shareCreatureType(selectedCards.get(0), selectedCards.get(1))) {
                throw new IllegalStateException("The selected creature cards must share a creature type");
            }
        }

        ReturnTargetCardsFromGraveyardToHandEffect typeLimitedEffect = targetValidationEffects == null
                ? null
                : targetValidationEffects.stream()
                .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                .map(ReturnTargetCardsFromGraveyardToHandEffect.class::cast)
                .filter(effect -> !effect.maxOnePerCardType().isEmpty())
                .findFirst()
                .orElse(null);
        if (typeLimitedEffect != null) {
            for (CardType cardType : typeLimitedEffect.maxOnePerCardType()) {
                long matchingTargets = cardIds.stream()
                        .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                        .filter(java.util.Objects::nonNull)
                        .filter(card -> card.hasType(cardType))
                        .count();
                if (matchingTargets > 1) {
                    throw new IllegalStateException("Cannot choose more than one "
                            + cardType.name().toLowerCase() + " card");
                }
            }
        }

        // "... from a single graveyard" (Scarab Feast): all chosen targets must share one graveyard.
        if (gameData.graveyardTargetOperation.singleGraveyard && cardIds.size() > 1) {
            UUID firstOwner = gameQueryService.findGraveyardOwnerById(gameData, cardIds.get(0));
            for (UUID cardId : cardIds) {
                if (!java.util.Objects.equals(firstOwner, gameQueryService.findGraveyardOwnerById(gameData, cardId))) {
                    throw new IllegalStateException("All targets must be in a single graveyard");
                }
            }
        }

        if (gameData.graveyardTargetOperation.cumulativeUpkeepPayment != null) {
            handleCumulativeUpkeepPayment(gameData, player, cardIds);
            return;
        }

        if (gameData.graveyardTargetOperation.controllerGraveyardPayment != null) {
            handleControllerGraveyardPayment(gameData, player, cardIds);
            return;
        }

        if (activeSpellGraveyardChoiceEffect != null) {
            gameData.graveyardTargetOperation.spellGraveyardCardIdsByEffect.put(
                    activeSpellGraveyardChoiceEffect, List.copyOf(cardIds));
            gameData.graveyardTargetOperation.activeSpellGraveyardChoiceEffect = null;
            if (gameData.graveyardTargetOperation.pendingSpellGraveyardChoiceEffects != null
                    && !gameData.graveyardTargetOperation.pendingSpellGraveyardChoiceEffects.isEmpty()) {
                gameData.interaction.clearAwaitingInput();
                if (graveyardTargetingService.beginNextSpellGraveyardChoice(gameData)) {
                    return;
                }
            }
        }

        IndependentlyTargetedGraveyardCardsEffect independentTargetEffect = pendingEffects == null
                ? null
                : EffectResolution.expandConditionalTargetingEffects(pendingEffects).stream()
                .filter(IndependentlyTargetedGraveyardCardsEffect.class::isInstance)
                .map(IndependentlyTargetedGraveyardCardsEffect.class::cast)
                .findFirst().orElse(null);
        if (independentTargetEffect != null
                && gameData.graveyardTargetOperation.independentTargetGroupIndex >= 0) {
            gameData.graveyardTargetOperation.independentTargetCardIds.addAll(cardIds);
            gameData.graveyardTargetOperation.independentTargetGroupSizes.add(cardIds.size());
            gameData.graveyardTargetOperation.independentTargetGroupIndex++;
            gameData.interaction.clearAwaitingInput();
            if (graveyardTargetingService.beginIndependentGraveyardSpellTargeting(
                    gameData, player.getId(), independentTargetEffect)) {
                return;
            }
            cardIds = List.copyOf(gameData.graveyardTargetOperation.independentTargetCardIds);
        }

        var exileMatchingContext = gameData.graveyardTargetOperation.resolutionTimeExileMatchingCardsResume;
        if (exileMatchingContext != null) {
            gameData.graveyardTargetOperation.resolutionTimeExileMatchingCardsResume = null;
            gameData.interaction.clearAwaitingInput();
            boolean librarySearchStarted = exileMatchingCardsSupport.completeGraveyardChoice(gameData,
                    exileMatchingContext.controllerId(), exileMatchingContext.filter(), cardIds);
            if (!librarySearchStarted) {
                if (gameData.pendingEffectResolutionEntry != null && !gameData.interaction.isAwaitingInput()) {
                    effectResolutionService.resolveEffectsFrom(gameData,
                            gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
                    if (gameData.interaction.isAwaitingInput()) {
                        return;
        }
    }

                inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            }
            return;
        }

        // As-enters "exile any number of creature cards from your graveyard" (CR 614.1c, Sutured
        // Ghoul): exile the chosen cards tracked with the entering permanent, then resume the entry
        // so its ETB triggers fire with the exiled cards already recorded.
        if (gameData.graveyardTargetOperation.asEntersExile != null) {
            var context = gameData.graveyardTargetOperation.asEntersExile;
            gameData.graveyardTargetOperation.asEntersExile = null;
            gameData.interaction.clearAwaitingInput();
            int exiledCount = 0;
            for (UUID cardId : cardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
                    exileService.exileCard(gameData, player.getId(), card, context.enteringPermanentId());
                    exiledCount++;
                    gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " exiles ", card, " from their graveyard."));
                }
            }
            battlefieldEntryService.applyAsEntersExileCounters(gameData, context.controllerId(),
                    context.enteringPermanentId(), exiledCount, context.countersPerCard());
            if (!context.counterTypes().isEmpty() && exiledCount > 0) {
                playerInputService.beginAsEntersCounterTypeChoice(gameData, context, exiledCount);
                return;
            }
            battlefieldEntryService.processCreatureETBEffects(gameData, context.controllerId(), context.card(),
                    context.targetId(), context.wasCastFromHand(), context.etbMode(), context.xValue(),
                    context.kicked(), context.targetIds());
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            }
            return;
        }

        // Resolution-time optional filtered exile with a life-loss rider: record the answer and
        // let the effect handler perform the zone change and rider on resumption.
        if (gameData.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeResume) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeResume = false;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeChoiceMade = true;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEachOpponentLosesLifeChosenCardId =
                    cardIds.isEmpty() ? null : cardIds.getFirst();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureResume) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureResume = false;
            gameData.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureChoiceMade = true;
            gameData.graveyardTargetOperation.resolutionTimeExileThenPutCounterOnTargetCreatureChosenCardId =
                    cardIds.isEmpty() ? null : cardIds.getFirst();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.graveyardTargetOperation.resolutionTimeExileThenEffectResume) {
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectResume = false;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade = true;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId =
                    cardIds.isEmpty() ? null : cardIds.getFirst();
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Resolution-time "exile up to one …" mid-resolution choices (Grixis Sojourners exile-only,
        // God-Pharaoh's Gift exile+4/4 black Zombie token copy with haste until EOT): the choice was
        // begun mid-resolution, so complete the effect and resume the paused ability rather than
        // pushing a new stack entry.
        if (gameData.graveyardTargetOperation.resolutionTimeExileResume
                || gameData.graveyardTargetOperation.resolutionTimeExileCreateZombieTokenCopyResume) {
            boolean createZombieTokenCopy =
                    gameData.graveyardTargetOperation.resolutionTimeExileCreateZombieTokenCopyResume;
            UUID sourcePermanentId = gameData.graveyardTargetOperation.sourcePermanentId;
            gameData.interaction.clearAwaitingInput();
            gameData.graveyardTargetOperation.resolutionTimeExileResume = false;
            gameData.graveyardTargetOperation.resolutionTimeExileCreateZombieTokenCopyResume = false;
            gameData.graveyardTargetOperation.sourcePermanentId = null;
            if (cardIds.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        player.getUsername() + " chooses not to exile a card from a graveyard."));
            } else {
                for (UUID cardId : cardIds) {
                    Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                    if (card != null) {
                        if (sourcePermanentId != null) {
                            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                            if (ownerId != null) {
                                permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
                                exileService.exileCard(gameData, ownerId, card, sourcePermanentId);
                            } else {
                                graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card);
                            }
                        } else {
                            graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card);
                        }
                        gameLogService.append(gameData, GameLog.textCardText(
                                player.getUsername() + " exiles ", card, " from a graveyard."));
                        if (createZombieTokenCopy && gameData.pendingEffectResolutionEntry != null) {
                            graveyardReturnSupport.createTokenCopyFromCard(
                                    gameData,
                                    gameData.pendingEffectResolutionEntry,
                                    card,
                                    List.of(CardSubtype.ZOMBIE),
                                    false,
                                    false,
                                    CardColor.BLACK,
                                    4,
                                    4,
                                    true,
                                    true);
                        }
                    }
                }
            }
            // The shared epilogue resumes the paused resolution (e.g. the cycling draw) and
            // auto-passes once no further input is needed.
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (gameData.resolvedMayTargetingEntry != null) {
            StackEntry pendingEntry = gameData.resolvedMayTargetingEntry;
            gameData.resolvedMayTargetingEntry = null;
            gameData.interaction.clearAwaitingInput();
            pendingEntry.setTargetCardIds(new ArrayList<>(cardIds));
            effectResolutionService.resolveEffectsFrom(gameData, pendingEntry,
                    gameData.pendingEffectResolutionIndex);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect forcedGraveyardExile =
                pendingEffects == null ? null : pendingEffects.stream()
                        .filter(com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect.class::isInstance)
                        .map(com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect.class::cast)
                        .filter(forced -> forced.forcedCost()
                                instanceof com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost)
                        .findFirst()
                        .orElse(null);
        if (forcedGraveyardExile != null) {
            gameData.interaction.clearAwaitingInput();
            for (UUID cardId : cardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                if (card != null && ownerId != null) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
                    exileService.exileCard(gameData, ownerId, card);
                }
            }
            gameData.graveyardTargetOperation.card = null;
            gameData.graveyardTargetOperation.controllerId = null;
            gameData.graveyardTargetOperation.effects = null;
            gameData.graveyardTargetOperation.sourcePermanentId = null;
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // Card pile separation (Boneyard Parley, Brilliant Ultimatum, Unesh, Curator of Destinies):
        // the separating player assigns cards to piles
        PendingTruthOrTaleCardChoice truthOrTaleChoice =
                gameData.peekPendingInteraction(PendingTruthOrTaleCardChoice.class);
        if (truthOrTaleChoice != null) {
            graveyardReturnSupport.completeTruthOrTaleCardChoice(gameData, cardIds);
            if (!gameData.interaction.isAwaitingInput()) {
                inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            }
            return;
        }

        PendingPileSeparation pileSeparation = gameData.peekPendingInteraction(PendingPileSeparation.class);
        if (pileSeparation != null && pileSeparation.cardPileMode()) {
            gameData.interaction.clearAwaitingInput();
            if (pileSeparation.disposition() == CardPileDisposition.PLAY_FROM_EXILE) {
                brilliantUltimatumSupport.completePileSeparationStep1(gameData, cardIds);
            } else if (pileSeparation.disposition() == CardPileDisposition.GIFTS_UNGIVEN
                    || pileSeparation.disposition() == CardPileDisposition.GIFTS_UNGIVEN_BATTLEFIELD_TAPPED) {
                // Gifts-style effects complete in one step: the chosen cards go to the controller's
                // graveyard and the remaining cards go to their configured destination.
                graveyardReturnSupport.completeGiftsUngivenChoice(gameData, cardIds,
                        pileSeparation.disposition() == CardPileDisposition.GIFTS_UNGIVEN_BATTLEFIELD_TAPPED);
                if (gameData.pendingEffectResolutionEntry != null && !gameData.interaction.isAwaitingInput()) {
                    effectResolutionService.resolveEffectsFrom(gameData,
                            gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
                    if (gameData.interaction.isAwaitingInput()) {
                        return;
                    }
                }
                inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            } else if (pileSeparation.disposition() == CardPileDisposition.DELIVER_UNTO_EVIL) {
                graveyardReturnSupport.completeDeliverUntoEvilChoice(gameData, cardIds);
                if (gameData.pendingEffectResolutionEntry != null && !gameData.interaction.isAwaitingInput()) {
                    effectResolutionService.resolveEffectsFrom(gameData,
                            gameData.pendingEffectResolutionEntry, gameData.pendingEffectResolutionIndex);
                    if (gameData.interaction.isAwaitingInput()) {
                        return;
                    }
                }
                inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            } else {
                // BATTLEFIELD (Boneyard Parley) and HAND (Unesh) both share step 1; step 2 branches.
                graveyardReturnSupport.completeCardPileSeparationStep1(gameData, cardIds);
            }
            return;
        }

        // Retrieve the pending info
        Card pendingCard = gameData.graveyardTargetOperation.card;
        UUID controllerId = gameData.graveyardTargetOperation.controllerId;
        StackEntryType pendingEntryType = gameData.graveyardTargetOperation.entryType;
        int pendingXValue = gameData.graveyardTargetOperation.xValue;
        CardSubtype pendingChosenCreatureType = gameData.graveyardTargetOperation.chosenCreatureType;
        UUID pendingTargetPlayerId = gameData.graveyardTargetOperation.targetPlayerId;
        boolean pendingFlashback = gameData.graveyardTargetOperation.flashback;
        boolean pendingGiftPromised = gameData.graveyardTargetOperation.giftPromised;
        Card pendingPhysicalCard = gameData.graveyardTargetOperation.physicalCard;
        boolean pendingAdventure = gameData.graveyardTargetOperation.castWithAdventure;
        boolean pendingKicked = gameData.graveyardTargetOperation.kicked;
        UUID pendingSourcePermanentId = gameData.graveyardTargetOperation.sourcePermanentId;
        Integer pendingTriggeringPermanentPowerAtTrigger =
                gameData.graveyardTargetOperation.triggeringPermanentPowerAtTrigger;
        String pendingChapterName = gameData.graveyardTargetOperation.chapterName;
        UUID pendingSpellCounterTargetId = gameData.graveyardTargetOperation.spellCounterTargetId;
        List<UUID> pendingPermanentTargetIds = gameData.graveyardTargetOperation.permanentTargetIds;
        Map<CardEffect, List<UUID>> pendingTargetCardIdsByEffect = new IdentityHashMap<>(
                gameData.graveyardTargetOperation.spellGraveyardCardIdsByEffect);
        List<Integer> pendingTargetCardGroupSizes = List.copyOf(
                gameData.graveyardTargetOperation.independentTargetGroupSizes);

        if (pendingCard != null
                && pendingCard.getMultiTargetConstraint() == MultiTargetConstraint.DIFFERENT_NAMES) {
            Set<String> names = new HashSet<>();
            for (UUID cardId : cardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && !names.add(card.getName())) {
                    throw new IllegalStateException("Chosen cards must have different names");
                }
            }
        }

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();
        gameData.graveyardTargetOperation.card = null;
        gameData.graveyardTargetOperation.controllerId = null;
        gameData.graveyardTargetOperation.effects = null;
        gameData.graveyardTargetOperation.entryType = null;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.chosenCreatureType = null;
        gameData.graveyardTargetOperation.anyNumber = false;
        gameData.graveyardTargetOperation.giftPromised = false;
        gameData.graveyardTargetOperation.singleGraveyard = false;
        gameData.graveyardTargetOperation.targetPlayerId = null;
        gameData.graveyardTargetOperation.flashback = false;
        gameData.graveyardTargetOperation.physicalCard = null;
        gameData.graveyardTargetOperation.castWithAdventure = false;
        gameData.graveyardTargetOperation.kicked = false;
        gameData.graveyardTargetOperation.sourcePermanentId = null;
        gameData.graveyardTargetOperation.triggeringPermanentPowerAtTrigger = null;
        gameData.graveyardTargetOperation.chapterName = null;
        gameData.graveyardTargetOperation.spellCounterTargetId = null;
        gameData.graveyardTargetOperation.permanentTargetIds = null;
        gameData.graveyardTargetOperation.pendingSpellGraveyardChoiceEffects = List.of();
        gameData.graveyardTargetOperation.activeSpellGraveyardChoiceEffect = null;
        gameData.graveyardTargetOperation.spellGraveyardCardIdsByEffect.clear();
        gameData.graveyardTargetOperation.independentTargetGroupIndex = -1;
        gameData.graveyardTargetOperation.independentTargetCardIds.clear();
        gameData.graveyardTargetOperation.independentTargetGroupSizes.clear();

        List<String> targetNames = new ArrayList<>();
        for (UUID cardId : cardIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null) {
                targetNames.add(card.getName());
            }
        }

        if (pendingEntryType != null) {
            // Spell casting — put spell on stack with targets. When a modal "both" mode also
            // countered a spell (Soul Manipulation), the counter's spell-on-stack target rides in
            // targetId alongside the graveyard return's targetCardIds. Otherwise targetId carries
            // the target-player (e.g. Memory's Journey) or nothing.
            UUID spellEntryTargetId = pendingSpellCounterTargetId != null
                    ? pendingSpellCounterTargetId : pendingTargetPlayerId;
            // A counter target rides on the stack (Zone.STACK) so on-resolution fizzle checks look for
            // it there rather than treating it as a missing permanent; the graveyard return reads its
            // own targetCardIds independently.
            Zone spellEntryTargetZone = pendingSpellCounterTargetId != null ? Zone.STACK : null;
            StackEntry spellEntry = new StackEntry(
                    pendingEntryType, pendingCard, controllerId, pendingCard.getName(),
                    new ArrayList<>(pendingEffects), pendingXValue, spellEntryTargetId,
                    null, Map.of(), spellEntryTargetZone, new ArrayList<>(cardIds),
                    pendingPermanentTargetIds == null ? List.of() : new ArrayList<>(pendingPermanentTargetIds)
            );
            spellEntry.setTargetCardIdsByEffect(pendingTargetCardIdsByEffect);
            spellEntry.setTargetCardGroupSizes(pendingTargetCardGroupSizes);
            spellEntry.setChosenCreatureType(pendingChosenCreatureType);
            if (pendingFlashback) {
                spellEntry.setCastWithFlashback(true);
            }
            spellEntry.setGiftPromised(pendingGiftPromised);
            if (pendingPhysicalCard != null) {
                spellEntry.setPhysicalCard(pendingPhysicalCard);
            }
            if (pendingAdventure) {
                spellEntry.setCastWithAdventure(true);
            }
            if (pendingKicked) {
                spellEntry.setKicked(true);
            }
            spellEntry.setSourceZone(pendingFlashback ? Zone.GRAVEYARD : Zone.HAND);
            gameData.stack.add(spellEntry);

            gameData.recordSpellCast(controllerId, pendingCard);
            gameData.priorityPassedBy.clear();

            
            gameLogService.append(gameData, GameLog.builder().text(gameData.playerIdToName.get(controllerId) + " casts ").card(pendingCard).text(" targeting " + String.join(", ", targetNames) + ".").build());
            log.info("Game {} - {} casts {} with {} graveyard targets", gameData.id, pendingCard.getName(),
                    pendingCard.getName(), cardIds.size());

            triggerCollectionService.checkSpellCastTriggers(gameData, pendingCard, controllerId,
                    !pendingFlashback);
            triggerCollectionService.checkTargetChoiceTriggers(gameData, spellEntry);
            inputCompletionService.publishStateAfterInput(gameData);
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
                        pendingXValue,
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
                        pendingXValue,
                        null,
                        null,
                        Map.of(),
                        null,
                        new ArrayList<>(cardIds),
                        List.of()
                );
            }
            if (pendingTargetPlayerId != null) {
                triggeredEntry.setTargetId(pendingTargetPlayerId);
            }
            if (pendingTriggeringPermanentPowerAtTrigger != null) {
                triggeredEntry.setTriggeringPermanentPowerAtTrigger(pendingTriggeringPermanentPowerAtTrigger);
            }
            gameData.stack.add(triggeredEntry);
            triggerCollectionService.checkTargetChoiceTriggers(gameData, triggeredEntry);

            if (cardIds.isEmpty()) {
                String triggerLog = description + " triggers targeting no cards.";
                gameLogService.append(gameData, GameLog.text(triggerLog));
            } else {
                String triggerLog = description + " triggers targeting " + String.join(", ", targetNames) + ".";
                gameLogService.append(gameData, GameLog.text(triggerLog));
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

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void validateMixedZoneSelection(GameData gameData, List<UUID> cardIds,
                                             BattlefieldAndGraveyardCardChoosingEffect effect) {
        int battlefieldTargets = 0;
        int graveyardTargets = 0;
        for (UUID cardId : cardIds) {
            if (findPermanentByCardId(gameData, cardId) != null) {
                battlefieldTargets++;
            } else if (gameQueryService.findCardInGraveyardById(gameData, cardId) != null) {
                graveyardTargets++;
            }
        }
        if (battlefieldTargets > effect.mixedZoneMaxBattlefieldTargets()) {
            throw new IllegalStateException("Too many battlefield permanents selected");
        }
        if (graveyardTargets > effect.mixedZoneMaxGraveyardTargets()) {
            throw new IllegalStateException("Too many graveyard cards selected");
        }
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(cardId)) {
                    return permanent;
                }
            }
        }
        return null;
    }

    private void handleCumulativeUpkeepPayment(GameData gameData, Player player, List<UUID> cardIds) {
        GraveyardTargetOperationState.CumulativeUpkeepPaymentContext context =
                gameData.graveyardTargetOperation.cumulativeUpkeepPayment;
        List<UUID> selectedCardIds = new ArrayList<>(context.selectedCardIds());
        selectedCardIds.addAll(cardIds);

        gameData.interaction.clearAwaitingInput();
        if (context.remainingPayments() > 1) {
            int remainingPayments = context.remainingPayments() - 1;
            gameData.graveyardTargetOperation.cumulativeUpkeepPayment =
                    new GraveyardTargetOperationState.CumulativeUpkeepPaymentContext(
                            context.sourceControllerId(), context.sourceCard(), context.sourcePermanentId(),
                            context.forcedCost(), context.cardsPerPayment(), remainingPayments, selectedCardIds);
            playerInputService.beginMultiGraveyardChoice(gameData, context.sourceControllerId(),
                    cumulativeUpkeepCandidateCards(gameData, selectedCardIds), context.cardsPerPayment(),
                    context.cardsPerPayment(), "Choose " + context.cardsPerPayment()
                            + " cards from a single graveyard for cumulative upkeep ("
                            + remainingPayments + " payment(s) remaining).");
            return;
        }

        List<Card> selectedCards = selectedCardIds.stream()
                .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                .toList();
        if (selectedCards.stream().anyMatch(java.util.Objects::isNull)) {
            resolveUnpaidCumulativeUpkeep(gameData, context);
            return;
        }

        gameData.graveyardTargetOperation.cumulativeUpkeepPayment = null;
        gameData.graveyardTargetOperation.singleGraveyard = false;
        for (int i = 0; i < selectedCardIds.size(); i++) {
            UUID cardId = selectedCardIds.get(i);
            Card card = selectedCards.get(i);
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
            gameData.playerDecks.get(ownerId).addLast(card);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", card, " on the bottom of its owner's library."));
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void handleControllerGraveyardPayment(GameData gameData, Player player, List<UUID> cardIds) {
        GraveyardTargetOperationState.ControllerGraveyardPaymentContext context =
                gameData.graveyardTargetOperation.controllerGraveyardPayment;
        gameData.interaction.clearAwaitingInput();
        gameData.graveyardTargetOperation.controllerGraveyardPayment = null;

        List<Card> selectedCards = cardIds.stream()
                .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                .toList();
        boolean allStillInControllerGraveyard = selectedCards.size() == context.count()
                && selectedCards.stream().allMatch(java.util.Objects::nonNull)
                && cardIds.stream().allMatch(cardId -> context.sourceControllerId().equals(
                        gameQueryService.findGraveyardOwnerById(gameData, cardId)));
        if (!allStillInControllerGraveyard) {
            resolveUnpaidControllerGraveyardPayment(gameData, context);
            return;
        }

        for (int i = 0; i < cardIds.size(); i++) {
            Card card = selectedCards.get(i);
            permanentRemovalService.removeCardFromGraveyardById(gameData, cardIds.get(i));
            gameData.playerDecks.get(context.sourceControllerId()).addLast(card);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", card, " on the bottom of their library."));
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private List<Card> cumulativeUpkeepCandidateCards(GameData gameData, List<UUID> selectedCardIds) {
        Set<UUID> excludedIds = new HashSet<>(selectedCardIds);
        List<Card> cards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(card -> !excludedIds.contains(card.getId()))
                    .forEach(cards::add);
        }
        return cards;
    }

    private void resolveUnpaidCumulativeUpkeep(GameData gameData,
                                                GraveyardTargetOperationState.CumulativeUpkeepPaymentContext context) {
        gameData.graveyardTargetOperation.cumulativeUpkeepPayment = null;
        gameData.graveyardTargetOperation.singleGraveyard = false;
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                context.sourceCard(),
                context.sourceControllerId(),
                context.sourceCard().getName() + "'s ability",
                List.of(context.forcedCost()),
                null,
                context.sourcePermanentId());
        destructionSupport.resolveForcedCostElseEffects(gameData, syntheticEntry, context.forcedCost());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void resolveUnpaidControllerGraveyardPayment(GameData gameData,
                                                          GraveyardTargetOperationState.ControllerGraveyardPaymentContext context) {
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                context.sourceCard(),
                context.sourceControllerId(),
                context.sourceCard().getName() + "'s ability",
                List.of(context.forcedCost()),
                null,
                context.sourcePermanentId());
        destructionSupport.resolveForcedCostElseEffects(gameData, syntheticEntry, context.forcedCost());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private boolean canAssignEachFilter(GameData gameData, List<UUID> cardIds,
                                        ReturnUpToOneOfEachFilterFromGraveyardToHandEffect effect) {
        List<Card> selectedCards = cardIds.stream()
                .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                .toList();
        if (selectedCards.stream().anyMatch(java.util.Objects::isNull)) {
            return false;
        }
        boolean[] usedFilters = new boolean[effect.filters().size()];
        UUID sourceCardId = gameData.graveyardTargetOperation.card == null
                ? null
                : gameData.graveyardTargetOperation.card.getId();
        return canAssignEachFilter(selectedCards, effect.filters(), 0, usedFilters, sourceCardId);
    }

    private boolean canAssignEachFilter(List<Card> selectedCards, List<CardPredicate> filters,
                                        int cardIndex, boolean[] usedFilters, UUID sourceCardId) {
        if (cardIndex == selectedCards.size()) {
            return true;
        }
        Card selectedCard = selectedCards.get(cardIndex);
        for (int filterIndex = 0; filterIndex < filters.size(); filterIndex++) {
            if (!usedFilters[filterIndex]
                    && predicateEvaluationService.matchesCardPredicate(
                    selectedCard, filters.get(filterIndex), sourceCardId)) {
                usedFilters[filterIndex] = true;
                if (canAssignEachFilter(selectedCards, filters, cardIndex + 1, usedFilters, sourceCardId)) {
                    return true;
                }
                usedFilters[filterIndex] = false;
            }
        }
        return false;
    }
}


