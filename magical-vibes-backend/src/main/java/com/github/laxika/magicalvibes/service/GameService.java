package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.ColorChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRegistry gameRegistry;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final CardViewFactory cardViewFactory;
    private final CombatService combatService;
    private final TurnProgressionService turnProgressionService;
    private final ColorChoiceHandlerService colorChoiceHandlerService;
    private final CardChoiceHandlerService cardChoiceHandlerService;
    private final PermanentChoiceHandlerService permanentChoiceHandlerService;
    private final GraveyardChoiceHandlerService graveyardChoiceHandlerService;
    private final MayAbilityHandlerService mayAbilityHandlerService;
    private final LibraryChoiceHandlerService libraryChoiceHandlerService;
    private final SpellCastingService spellCastingService;
    private final StackResolutionService stackResolutionService;
    private final AbilityActivationService abilityActivationService;
    private final MulliganService mulliganService;
    private final ReconnectionService reconnectionService;

    public void passPriority(GameData gameData, Player player) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            if (gameData.awaitingInput != null) {
                throw new IllegalStateException("Cannot pass priority while awaiting input");
            }

            UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
            if (priorityHolder == null || !priorityHolder.equals(player.getId())) {
                throw new IllegalStateException("You do not have priority");
            }

            gameData.priorityPassedBy.add(player.getId());
            log.info("Game {} - {} passed priority on step {} (passed: {}/2)",
                    gameData.id, player.getUsername(), gameData.currentStep, gameData.priorityPassedBy.size());

            if (gameData.priorityPassedBy.size() >= 2) {
                if (!gameData.stack.isEmpty()) {
                    stackResolutionService.resolveTopOfStack(gameData);
                } else {
                    turnProgressionService.advanceStep(gameData);
                }
            } else {
                gameBroadcastService.broadcastGameState(gameData);
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void advanceStep(GameData gameData) {
        turnProgressionService.advanceStep(gameData);
    }

    public JoinGame getJoinGame(GameData data, UUID playerId) {
        return toJoinGame(data, playerId);
    }

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        synchronized (gameData) {
            reconnectionService.resendAwaitingInput(gameData, playerId);
        }
    }

    public void keepHand(GameData gameData, Player player) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.keepHand(gameData, player);
        }
    }

    public void bottomCards(GameData gameData, Player player, List<Integer> cardIndices) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.bottomCards(gameData, player, cardIndices);
        }
    }

    public void mulligan(GameData gameData, Player player) {
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.mulligan(gameData, player);
        }
    }

    private JoinGame toJoinGame(GameData data, UUID playerId) {
        List<CardView> hand = playerId != null
                ? data.playerHands.getOrDefault(playerId, List.of()).stream().map(cardViewFactory::create).toList()
                : List.of();
        int mulliganCount = playerId != null ? data.mulliganCounts.getOrDefault(playerId, 0) : 0;
        Map<String, Integer> manaPool = gameBroadcastService.getManaPool(data, playerId);
        List<TurnStep> autoStopSteps = playerId != null && data.playerAutoStopSteps.containsKey(playerId)
                ? new ArrayList<>(data.playerAutoStopSteps.get(playerId))
                : List.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
        return new JoinGame(
                data.id,
                data.gameName,
                data.status,
                new ArrayList<>(data.playerNames),
                new ArrayList<>(data.orderedPlayerIds),
                new ArrayList<>(data.gameLog),
                data.currentStep,
                data.activePlayerId,
                data.turnNumber,
                gameQueryService.getPriorityPlayerId(data),
                hand,
                mulliganCount,
                gameBroadcastService.getDeckSizes(data),
                gameBroadcastService.getHandSizes(data),
                gameBroadcastService.getBattlefields(data),
                manaPool,
                autoStopSteps,
                gameBroadcastService.getLifeTotals(data),
                gameBroadcastService.getStackViews(data),
                gameBroadcastService.getGraveyardViews(data)
        );
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments) {
        synchronized (gameData) {
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, List.of(), List.of(), false);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds) {
        synchronized (gameData) {
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, false);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard) {
        synchronized (gameData) {
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, damageAssignments, targetPermanentIds, convokeCreatureIds, fromGraveyard);
        }
    }

    public void tapPermanent(GameData gameData, Player player, int permanentIndex) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            abilityActivationService.tapPermanent(gameData, player, permanentIndex);
        }
    }

    public void sacrificePermanent(GameData gameData, Player player, int permanentIndex, UUID targetPermanentId) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            abilityActivationService.sacrificePermanent(gameData, player, permanentIndex, targetPermanentId);
        }
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetPermanentId, TargetZone targetZone) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            abilityActivationService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, targetZone);
        }
    }

    public void setAutoStops(GameData gameData, Player player, List<TurnStep> stops) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        synchronized (gameData) {
            Set<TurnStep> stopSet = ConcurrentHashMap.newKeySet();
            stopSet.addAll(stops);
            stopSet.add(TurnStep.PRECOMBAT_MAIN);
            stopSet.add(TurnStep.POSTCOMBAT_MAIN);
            gameData.playerAutoStopSteps.put(player.getId(), stopSet);
            gameBroadcastService.broadcastGameState(gameData);
        }
    }

    // ===== Delegated user input handlers =====

    public void handleColorChosen(GameData gameData, Player player, String colorName) {
        synchronized (gameData) {
            colorChoiceHandlerService.handleColorChosen(gameData, player, colorName);
        }
    }

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            cardChoiceHandlerService.handleCardChosen(gameData, player, cardIndex);
        }
    }

    public void handlePermanentChosen(GameData gameData, Player player, UUID permanentId) {
        synchronized (gameData) {
            permanentChoiceHandlerService.handlePermanentChosen(gameData, player, permanentId);
        }
    }

    public void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            graveyardChoiceHandlerService.handleGraveyardCardChosen(gameData, player, cardIndex);
        }
    }

    public void handleMultiplePermanentsChosen(GameData gameData, Player player, List<UUID> permanentIds) {
        synchronized (gameData) {
            permanentChoiceHandlerService.handleMultiplePermanentsChosen(gameData, player, permanentIds);
        }
    }

    public void handleMultipleGraveyardCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        synchronized (gameData) {
            if (gameData.awaitingInput == AwaitingInput.LIBRARY_REVEAL_CHOICE) {
                libraryChoiceHandlerService.handleLibraryRevealChoice(gameData, player, cardIds);
            } else {
                graveyardChoiceHandlerService.handleMultipleGraveyardCardsChosen(gameData, player, cardIds);
            }
        }
    }

    public void handleMayAbilityChosen(GameData gameData, Player player, boolean accepted) {
        synchronized (gameData) {
            mayAbilityHandlerService.handleMayAbilityChosen(gameData, player, accepted);
        }
    }

    public void handleLibraryCardsReordered(GameData gameData, Player player, List<Integer> cardOrder) {
        synchronized (gameData) {
            libraryChoiceHandlerService.handleLibraryCardsReordered(gameData, player, cardOrder);
        }
    }

    public void handleHandTopBottomChosen(GameData gameData, Player player, int handCardIndex, int topCardIndex) {
        synchronized (gameData) {
            libraryChoiceHandlerService.handleHandTopBottomChosen(gameData, player, handCardIndex, topCardIndex);
        }
    }

    public void handleLibraryCardChosen(GameData gameData, Player player, int cardIndex) {
        synchronized (gameData) {
            libraryChoiceHandlerService.handleLibraryCardChosen(gameData, player, cardIndex);
        }
    }

    // ===== Combat wrapper methods =====

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        synchronized (gameData) {
            turnProgressionService.handleCombatResult(combatService.declareAttackers(gameData, player, attackerIndices), gameData);
        }
    }

    public void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        synchronized (gameData) {
            turnProgressionService.handleCombatResult(combatService.declareBlockers(gameData, player, blockerAssignments), gameData);
        }
    }

    // ===== Thin delegates for test API =====

    public boolean isCreature(GameData gameData, Permanent permanent) {
        return gameQueryService.isCreature(gameData, permanent);
    }

    public int getEffectivePower(GameData gameData, Permanent permanent) {
        return gameQueryService.getEffectivePower(gameData, permanent);
    }

    public int getEffectiveToughness(GameData gameData, Permanent permanent) {
        return gameQueryService.getEffectiveToughness(gameData, permanent);
    }

    public boolean hasKeyword(GameData gameData, Permanent permanent, Keyword keyword) {
        return gameQueryService.hasKeyword(gameData, permanent, keyword);
    }
}
