package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.PlayCardRequestDispatchService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import lombok.extern.slf4j.Slf4j;

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
 * never learns about message handlers or connections. The cast dispatch is NOT mirrored here:
 * both this adapter and the backend handler share {@link PlayCardRequestDispatchService}, so the
 * two can no longer drift apart field-by-field. Like the backend handler,
 * each method swallows {@link IllegalArgumentException}/{@link IllegalStateException} (an illegal
 * action is simply a no-op for the AI). The {@link Connection} parameter is accepted for call-site
 * symmetry with the broadcast pipeline but is unused here — the acting player is fixed.
 */
@Slf4j
public class AiGameActions {

    private final UUID gameId;
    private final Player aiPlayer;
    private final GameService gameService;
    private final GameRegistry gameRegistry;
    private final PlayCardRequestDispatchService playCardRequestDispatchService;

    public AiGameActions(UUID gameId, Player aiPlayer, GameService gameService, GameRegistry gameRegistry) {
        this.gameId = gameId;
        this.aiPlayer = aiPlayer;
        this.gameService = gameService;
        this.gameRegistry = gameRegistry;
        // Stateless; constructed directly because AiGameActions is a per-game object, not a bean.
        this.playCardRequestDispatchService = new PlayCardRequestDispatchService(gameService);
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
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected passPriority in game {}: {}", gameId, e.getMessage());
        }
    }

    public void handleKeepHand(Connection connection, KeepHandRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.keepHand(gameData, aiPlayer);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected keepHand in game {}: {}", gameId, e.getMessage());
        }
    }

    public void handleMulligan(Connection connection, MulliganRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.mulligan(gameData, aiPlayer);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected mulligan in game {}: {}", gameId, e.getMessage());
        }
    }

    public void handleBottomCards(Connection connection, BottomCardsRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.bottomCards(gameData, aiPlayer, request.cardIndices());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected bottomCards in game {}: {}", gameId, e.getMessage());
        }
    }

    public void handlePlayCard(Connection connection, PlayCardRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            playCardRequestDispatchService.dispatch(gameData, aiPlayer, request);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Illegal action is a no-op for the AI; logged so fuzz failures show the engine's reason
            log.info("AI: engine rejected playCard (index={}) in game {}: {}", request.cardIndex(), gameId, e.getMessage());
        }
    }

    public void handleTapPermanent(Connection connection, TapPermanentRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.tapPermanent(gameData, aiPlayer, request.permanentIndex());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected tapPermanent (index={}) in game {}: {}", request.permanentIndex(), gameId, e.getMessage());
        }
    }

    /**
     * Pure legality query — asks the engine whether the AI player could activate the given
     * ability right now, with mana affordability measured against {@code manaPool} (typically
     * the AI's virtual pool of producible mana). Mutates nothing and swallows nothing: the
     * engine's answer is the AI's answer, so AI strategies share the engine's legality rules
     * instead of re-implementing them.
     */
    public boolean canActivateAbility(GameData gameData, Permanent permanent, int abilityIndex, ManaPool manaPool) {
        return gameService.canActivateAbility(gameData, aiPlayer.getId(), permanent, abilityIndex, manaPool);
    }

    /** Returns the activated abilities available on a permanent, in engine {@code abilityIndex} order. */
    public List<ActivatedAbility> getEffectiveActivatedAbilities(GameData gameData, Permanent permanent) {
        return gameService.getEffectiveActivatedAbilities(gameData, permanent);
    }

    public void handleActivateAbility(Connection connection, ActivateAbilityRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.activateAbility(gameData, aiPlayer, request.permanentIndex(), request.abilityIndex(), request.xValue(),
                    request.targetId(), request.targetZone(), request.targetIds(), request.damageAssignments());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected activateAbility (permanentIndex={}, abilityIndex={}) in game {}: {}",
                    request.permanentIndex(), request.abilityIndex(), gameId, e.getMessage());
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
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected declareAttackers in game {}: {}", gameId, e.getMessage());
        }
    }

    public void handleDeclareBlockers(Connection connection, DeclareBlockersRequest request) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.declareBlockers(gameData, aiPlayer, request.blockerAssignments());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected declareBlockers in game {}: {}", gameId, e.getMessage());
        }
    }

    /**
     * Applies the AI's answer to the active pending interaction — the AI-side twin of the
     * backend's single interaction-answer route. The {@link InteractionAnswer} shape carries
     * the payload; the engine's registry routes it to the active interaction's handler.
     */
    public void answerInteraction(Connection connection, InteractionAnswer answer) {
        GameData gameData = game();
        if (gameData == null) return;
        try {
            gameService.handleInteractionAnswer(gameData, aiPlayer, answer);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected {} in game {}: {}",
                    answer.getClass().getSimpleName(), gameId, e.getMessage());
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
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.info("AI: engine rejected combatDamageAssigned in game {}: {}", gameId, e.getMessage());
        }
    }
}
