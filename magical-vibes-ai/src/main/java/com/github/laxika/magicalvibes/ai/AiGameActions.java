package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.ChosenFromListRequest;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.GraveyardCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.HandTopBottomChosenRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.LibraryCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MayAbilityChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.MultipleCardsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MultiplePermanentsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PermanentChosenRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsRequest;
import com.github.laxika.magicalvibes.networking.message.ScryCompletedRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.networking.message.XValueChosenRequest;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The AI's adapter onto the engine's game-action API ({@link GameService}). It mirrors the
 * in-game subset of player actions the AI performs, unwrapping the wire request records the AI
 * already builds and invoking the matching {@link GameService} method for the (fixed) AI player.
 *
 * <p>This is the AI-side equivalent of what the backend {@code GameMessageHandler} does for human
 * players — but it lives in the AI module and speaks only to {@link GameService}, so the engine
 * never learns about message handlers, connections, or the wire protocol. Like the backend handler,
 * each method swallows {@link IllegalArgumentException}/{@link IllegalStateException} (an illegal
 * action is simply a no-op for the AI). The {@link Connection} parameter is accepted for call-site
 * symmetry with the broadcast pipeline but is unused here — the acting player is fixed.
 */
public class AiGameActions {

    private final UUID gameId;
    private final Player aiPlayer;
    private final GameService gameService;
    private final GameRegistry gameRegistry;

    public AiGameActions(UUID gameId, Player aiPlayer, GameService gameService, GameRegistry gameRegistry) {
        this.gameId = gameId;
        this.aiPlayer = aiPlayer;
        this.gameService = gameService;
        this.gameRegistry = gameRegistry;
    }

    private GameData game() {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null || gameData.status == GameStatus.FINISHED) {
            return null;
        }
        return gameData;
    }

    public void handlePassPriority(Connection connection, PassPriorityRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.passPriority(gameData, aiPlayer);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleKeepHand(Connection connection, KeepHandRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.keepHand(gameData, aiPlayer);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleMulligan(Connection connection, MulliganRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.mulligan(gameData, aiPlayer);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleBottomCards(Connection connection, BottomCardsRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.bottomCards(gameData, aiPlayer, request.cardIndices());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handlePlayCard(Connection connection, PlayCardRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            if (Boolean.TRUE.equals(request.fromLibraryTop())) {
                gameService.playCardFromLibraryTop(gameData, aiPlayer, request.xValue(), request.targetId());
            } else if (Boolean.TRUE.equals(request.flashback())) {
                CardType chosenGraveyardType = request.chosenGraveyardType() != null
                        ? CardType.valueOf(request.chosenGraveyardType()) : null;
                gameService.playFlashbackSpell(gameData, aiPlayer, request.cardIndex(), request.xValue(), request.targetId(),
                        request.targetIds() != null ? request.targetIds() : List.of(),
                        request.exileGraveyardCardIndices(), chosenGraveyardType);
            } else if (request.fromExileCardId() != null) {
                gameService.playCardFromExile(gameData, aiPlayer, request.fromExileCardId(), request.xValue(), request.targetId());
            } else if (request.alternateCostSacrificePermanentIds() != null && !request.alternateCostSacrificePermanentIds().isEmpty()) {
                gameService.playCard(gameData, aiPlayer, request.cardIndex(), request.xValue(), request.targetId(), request.damageAssignments(),
                        request.targetIds() != null ? request.targetIds() : List.of(),
                        request.convokeCreatureIds() != null ? request.convokeCreatureIds() : List.of(),
                        Boolean.TRUE.equals(request.fromGraveyard()), request.sacrificePermanentId(), request.phyrexianLifeCount(),
                        request.alternateCostSacrificePermanentIds());
            } else if (request.exileGraveyardCardIndices() != null && !request.exileGraveyardCardIndices().isEmpty()) {
                gameService.playCard(gameData, aiPlayer, request.cardIndex(), request.xValue(), request.targetId(), request.damageAssignments(),
                        request.targetIds() != null ? request.targetIds() : List.of(),
                        request.convokeCreatureIds() != null ? request.convokeCreatureIds() : List.of(),
                        Boolean.TRUE.equals(request.fromGraveyard()), request.sacrificePermanentId(), request.phyrexianLifeCount(),
                        null, null, request.exileGraveyardCardIndices());
            } else if (request.exileGraveyardCardIndex() != null) {
                gameService.playCard(gameData, aiPlayer, request.cardIndex(), request.xValue(), request.targetId(), request.damageAssignments(),
                        request.targetIds() != null ? request.targetIds() : List.of(),
                        request.convokeCreatureIds() != null ? request.convokeCreatureIds() : List.of(),
                        Boolean.TRUE.equals(request.fromGraveyard()), request.sacrificePermanentId(), request.phyrexianLifeCount(),
                        null, request.exileGraveyardCardIndex());
            } else {
                gameService.playCard(gameData, aiPlayer, request.cardIndex(), request.xValue(), request.targetId(), request.damageAssignments(),
                        request.targetIds() != null ? request.targetIds() : List.of(),
                        request.convokeCreatureIds() != null ? request.convokeCreatureIds() : List.of(),
                        Boolean.TRUE.equals(request.fromGraveyard()), request.sacrificePermanentId(), request.phyrexianLifeCount(),
                        null, null, null, Boolean.TRUE.equals(request.kicked()));
            }
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleTapPermanent(Connection connection, TapPermanentRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.tapPermanent(gameData, aiPlayer, request.permanentIndex());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleActivateAbility(Connection connection, ActivateAbilityRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.activateAbility(gameData, aiPlayer, request.permanentIndex(), request.abilityIndex(), request.xValue(),
                    request.targetId(), request.targetZone(), request.targetIds(), request.damageAssignments());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleDeclareAttackers(Connection connection, DeclareAttackersRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            Map<Integer, UUID> attackTargets = null;
            if (request.attackTargets() != null) {
                attackTargets = new HashMap<>();
                for (var entry : request.attackTargets().entrySet()) {
                    attackTargets.put(entry.getKey(), UUID.fromString(entry.getValue()));
                }
            }
            gameService.declareAttackers(gameData, aiPlayer, request.attackerIndices(), attackTargets);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleDeclareBlockers(Connection connection, DeclareBlockersRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.declareBlockers(gameData, aiPlayer, request.blockerAssignments());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleCardChosen(Connection connection, CardChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleCardChosen(gameData, aiPlayer, request.cardIndex());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleGraveyardCardChosen(Connection connection, GraveyardCardChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleGraveyardCardChosen(gameData, aiPlayer, request.cardIndex());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handlePermanentChosen(Connection connection, PermanentChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handlePermanentChosen(gameData, aiPlayer, request.permanentId());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleMultiplePermanentsChosen(Connection connection, MultiplePermanentsChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleMultiplePermanentsChosen(gameData, aiPlayer, request.permanentIds());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleMultipleCardsChosen(Connection connection, MultipleCardsChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleMultipleCardsChosen(gameData, aiPlayer, request.cardIds());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleListChoice(Connection connection, ChosenFromListRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleListChoice(gameData, aiPlayer, request.choice());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleMayAbilityChosen(Connection connection, MayAbilityChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleMayAbilityChosen(gameData, aiPlayer, request.accepted());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleXValueChosen(Connection connection, XValueChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleXValueChosen(gameData, aiPlayer, request.chosenValue());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleScryCompleted(Connection connection, ScryCompletedRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleScryCompleted(gameData, aiPlayer, request.topCardOrder(), request.bottomCardOrder());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleLibraryCardsReordered(Connection connection, ReorderLibraryCardsRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleLibraryCardsReordered(gameData, aiPlayer, request.cardOrder());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleLibraryCardChosen(Connection connection, LibraryCardChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleLibraryCardChosen(gameData, aiPlayer, request.cardIndex());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleHandTopBottomChosen(Connection connection, HandTopBottomChosenRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleHandTopBottomChosen(gameData, aiPlayer, request.handCardIndex(), request.topCardIndex());
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    public void handleCombatDamageAssigned(Connection connection, CombatDamageAssignedRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            Map<UUID, Integer> assignments = new HashMap<>();
            for (var entry : request.damageAssignments().entrySet()) {
                assignments.put(UUID.fromString(entry.getKey()), entry.getValue());
            }
            gameService.handleCombatDamageAssigned(gameData, aiPlayer, request.attackerIndex(), assignments);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }
}
