package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndStep;
import com.github.laxika.magicalvibes.model.action.ExileTokenAtEndStep;
import com.github.laxika.magicalvibes.model.action.ExileTokenAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;

import com.github.laxika.magicalvibes.model.PendingKarnRestart;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.PendingKnowledgePoolCast;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KarnRestartGameEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class KarnRestartGameEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KarnRestartGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Step 1: Collect non-Aura permanent cards exiled with Karn
        List<Card> karnExiledCards = new ArrayList<>();
        if (sourcePermanentId != null) {
            List<Card> pool = gameData.getCardsExiledByPermanent(sourcePermanentId);
            for (Card card : pool) {
                boolean isAura = card.getSubtypes().contains(CardSubtype.AURA);
                boolean isPermanent = !card.hasType(CardType.INSTANT)
                        && !card.hasType(CardType.SORCERY);
                if (isPermanent && !isAura) {
                    karnExiledCards.add(card);
                }
            }
        }

        String logEntry = controllerName + " restarts the game with Karn Liberated!";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} restarts the game with Karn Liberated ({} cards saved)",
                gameData.id, controllerName, karnExiledCards.size());

        // Step 2: Gather all non-token cards from all zones for each player into their library.
        // First, collect owned battlefield cards for each player (must happen before clearing
        // battlefields so stolen creature lookup works across all controllers).
        Map<UUID, List<Card>> ownedBattlefieldCards = new HashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> owned = new ArrayList<>();
            for (UUID ctrlId : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(ctrlId);
                if (bf == null) continue;
                for (Permanent perm : bf) {
                    if (!perm.getCard().isToken()) {
                        UUID ownerId = gameData.stolenCreatures.getOrDefault(perm.getId(), ctrlId);
                        if (ownerId.equals(playerId)) {
                            owned.add(perm.getOriginalCard());
                        }
                    }
                }
            }
            ownedBattlefieldCards.put(playerId, owned);
        }

        // Clear all battlefields now that ownership has been resolved
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf != null) bf.clear();
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> allCards = new ArrayList<>();

            // From deck
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null) {
                allCards.addAll(deck);
                deck.clear();
            }

            // From hand
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand != null) {
                allCards.addAll(hand);
                hand.clear();
            }

            // From graveyard
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                allCards.addAll(graveyard);
                graveyard.clear();
            }

            // From exile
            List<Card> exile = gameData.getPlayerExiledCards(playerId);
            // Remove cards that Karn is keeping — they stay in exile
            List<Card> exileToReturn = new ArrayList<>(exile);
            exileToReturn.removeAll(karnExiledCards);
            allCards.addAll(exileToReturn);
            // Remove the non-Karn cards from the unified exile list
            for (Card c : exileToReturn) {
                gameData.removeFromExile(c.getId());
            }

            // From battlefield (already collected above)
            allCards.addAll(ownedBattlefieldCards.get(playerId));

            // Remove Karn's saved cards from the pool (they stay in exile)
            allCards.removeAll(karnExiledCards);

            // Shuffle into library
            Collections.shuffle(allCards);
            deck = gameData.playerDecks.get(playerId);
            if (deck == null) {
                deck = Collections.synchronizedList(new ArrayList<>());
                gameData.playerDecks.put(playerId, deck);
            }
            deck.addAll(allCards);
        }

        // Step 3: Clear all game state
        gameData.stack.clear();
        gameData.clearAllSourceTracking();
        gameData.stolenCreatures.clear();
        gameData.floatingEffects.clear();
        gameData.clearDelayedActions(PendingExileReturn.class);
        gameData.exileReturnOnPermanentLeave.clear();
        gameData.sourceLinkedAnimations.clear();
        gameData.clearDelayedActions(ExileTokenAtEndStep.class);
        gameData.clearDelayedActions(SacrificeAtEndStep.class);
        gameData.pendingMayAbilities.clear();
        gameData.clearPendingInteractions(PermanentChoiceContext.DeathTriggerTarget.class);
        gameData.clearPendingInteractions(PermanentChoiceContext.DiscardTriggerAnyTarget.class);
        gameData.clearPendingInteractions(PermanentChoiceContext.AttackTriggerTarget.class);
        gameData.clearPendingInteractions(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class);
        gameData.clearPendingInteractions(PermanentChoiceContext.EmblemTriggerTarget.class);
        gameData.clearPendingInteractions(PermanentChoiceContext.UpkeepAnyTargetTrigger.class);
        gameData.clearPendingInteractions(PermanentChoiceContext.UpkeepPlayerTargetTrigger.class);
        gameData.clearPendingInteractions(PermanentChoiceContext.UpkeepMultiPlayerTargetTrigger.class);
        gameData.clearPendingInteractions(PermanentChoiceContext.UpkeepCopyTriggerTarget.class);
        gameData.emblems.clear();
        gameData.extraTurns.clear();
        gameData.pendingLibraryBottomReorders.clear();
        gameData.openingHandRevealTriggers.clear();
        gameData.openingHandManaTriggers.clear();
        gameData.playersWhoCastFirstSpellInGame.clear();
        gameData.playersWithNoMaximumHandSize.clear();
        gameData.priorityPassedBy.clear();
        gameData.clearDelayedActions(SacrificeAtEndOfCombat.class);
        gameData.clearDelayedActions(ExileTokenAtEndOfCombat.class);
        gameData.permanentsPreventedFromDealingDamage.clear();
        gameData.drawReplacementTargetToController.clear();
        gameData.playerSpellsCantBeCounteredByColorsThisTurn.clear();
        gameData.playerCreaturesCantBeTargetedByColorsThisTurn.clear();
        gameData.playersSilencedThisTurn.clear();
        gameData.activatedAbilityUsesThisTurn.clear();
        gameData.permanentAbilityResolutionsThisTurn.clear();
        gameData.pendingTurnControl.clear();
        gameData.combatDamageToPlayersThisTurn.clear();
        gameData.combatDamageSourceSubtypesThisTurn.clear();
        gameData.combatDamageSourcesWithChangelingThisTurn.clear();
        gameData.paidSearchTaxPermanentIds.clear();
        gameData.combatDamagePlayerAssignments.clear();
        gameData.combatDamagePendingIndices.clear();
        gameData.playersAttemptedDrawFromEmptyLibrary.clear();
        gameData.preventDamageFromColors.clear();

        gameData.pendingEffectResolutionEntry = null;
        gameData.pendingEffectResolutionIndex = 0;
        gameData.chosenXValue = null;
        gameData.pendingAbilityActivation = null;
        gameData.clearPendingInteractions(PendingKnowledgePoolCast.class);
        gameData.pendingReturnToHandOnDiscardType = null;
        gameData.pendingTransformOnCreatureDiscard = null;
        gameData.combatDamageRedirectTarget = null;
        gameData.globalDamagePreventionShield = 0;
        gameData.damageRedirectShields.clear();
        gameData.preventAllCombatDamage = false;
        gameData.preventAllDamageToAllCreatures = false;
        gameData.combatDamageExemptPredicate = null;
        gameData.allPermanentsEnterTappedThisTurn = false;
        gameData.endTurnRequested = false;
        gameData.additionalCombatMainPhasePairs = 0;
        gameData.discardCausedByOpponent = false;
        gameData.cleanupDiscardPending = false;
        gameData.mindControlledPlayerId = null;
        gameData.mindControllerPlayerId = null;
        gameData.pendingSearchContext = null;
        gameData.pendingETBDamageAssignments = Map.of();
        gameData.combatDamageFirstStrikeStepComplete = false;
        gameData.combatDamagePhase1Complete = false;
        gameData.combatDamagePhase1State = null;

        for (UUID playerId : gameData.orderedPlayerIds) {
            gameData.playerLifeTotals.put(playerId, 20);
            gameData.playerPoisonCounters.put(playerId, 0);
            gameData.playerManaPools.put(playerId, new ManaPool());
            gameData.landsPlayedThisTurn.put(playerId, 0);
            gameData.additionalLandsThisTurn.put(playerId, 0);
            gameData.cardsDrawnThisTurn.put(playerId, 0);
            gameData.playerDamagePreventionShields.put(playerId, 0);
            gameData.permanentsEnteredBattlefieldThisTurn.put(playerId, Collections.synchronizedList(new ArrayList<>()));
            gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.put(playerId, ConcurrentHashMap.newKeySet());
            gameData.creatureDeathCountThisTurn.put(playerId, 0);
            gameData.creatureCardsDamagedThisTurnBySourcePermanent.put(playerId, ConcurrentHashMap.newKeySet());
            gameData.creatureGivingControllerPoisonOnDeathThisTurn.clear();
            gameData.playerSourceDamagePreventionIds.put(playerId, ConcurrentHashMap.newKeySet());
            gameData.playerColorDamagePreventionCount.put(playerId, new ConcurrentHashMap<>());

            if (gameData.playerBattlefields.get(playerId) == null) {
                gameData.playerBattlefields.put(playerId, gameData.newBattlefieldList());
            }
        }

        gameData.interaction.clearAwaitingInput();
        gameData.turnNumber = 1;

        // Step 4: Each player draws 7 cards (CR 726 — pregame procedure)
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            int toDraw = Math.min(7, deck.size());
            for (int i = 0; i < toDraw; i++) {
                gameData.addCardToHand(playerId, deck.removeFirst());
            }
            String drawLog = gameData.playerIdToName.get(playerId) + " draws 7 cards.";
            gameBroadcastService.logAndBroadcast(gameData, drawLog);
        }

        // Step 5: Controller goes first (CR 726)
        gameData.startingPlayerId = controllerId;

        // Step 6: Enter mulligan phase. After mulligans complete, Karn's exiled cards
        // will be put onto the battlefield (handled by MulliganService.startGame).
        gameData.queueInteraction(new PendingKarnRestart(karnExiledCards, controllerId));

        for (UUID playerId : gameData.orderedPlayerIds) {
            gameData.mulliganCounts.put(playerId, 0);
        }
        gameData.playerKeptHand.clear();
        gameData.playerNeedsToBottom.clear();
        gameData.status = GameStatus.MULLIGAN;

        gameBroadcastService.logAndBroadcast(gameData, "Mulligan phase — decide to keep or mulligan.");
        gameBroadcastService.broadcastGameState(gameData);

        // Kick off the new mulligan round for message-driven clients (AI players decide on
        // MULLIGAN_RESOLVED; without this the restarted game waits forever on their answer)
        sessionManager.sendToPlayers(gameData.orderedPlayerIds,
                new MulliganResolvedMessage(controllerName, false, 0));
    }
}
