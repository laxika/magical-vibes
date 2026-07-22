package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class GameOutcomeService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final GameRegistry gameRegistry;
    private final DraftRegistry draftRegistry;
    private final ObjectProvider<TournamentResultHandler> tournamentResultHandler;
    private final GameTimeoutService gameTimeoutService;
    private final LichsMirrorResetService lichsMirrorResetService;

    public GameOutcomeService(GameQueryService gameQueryService,
                              GameBroadcastService gameBroadcastService,
                              SessionManager sessionManager,
                              GameRegistry gameRegistry,
                              DraftRegistry draftRegistry,
                              ObjectProvider<TournamentResultHandler> tournamentResultHandler,
                              @Lazy GameTimeoutService gameTimeoutService,
                              @Lazy LichsMirrorResetService lichsMirrorResetService) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.sessionManager = sessionManager;
        this.gameRegistry = gameRegistry;
        this.draftRegistry = draftRegistry;
        this.tournamentResultHandler = tournamentResultHandler;
        this.gameTimeoutService = gameTimeoutService;
        this.lichsMirrorResetService = lichsMirrorResetService;
    }

    /**
     * Lich's Mirror hook: if {@code losingPlayerId} controls a permanent that replaces their loss
     * with a game reset, performs the reset and returns {@code true}. Callers about to finish the
     * game because this player lost a rules-based loss (life/poison, empty library, a "you lose the
     * game" effect) must consult this first and skip the loss when it returns {@code true}.
     */
    public boolean replaceLossWithGameReset(GameData gameData, UUID losingPlayerId) {
        return lichsMirrorResetService.tryReplaceLoss(gameData, losingPlayerId);
    }

    public boolean checkWinCondition(GameData gameData) {
        // CR 704.3 / 104.3b — state-based actions (including loss from life <= 0) are only checked
        // when a player would receive priority, i.e. after a spell or ability finishes resolving.
        // While a stack entry's effect list is mid-resolution, defer: a controller momentarily at
        // 0 life between two effects of the same spell survives if a later effect restores them.
        // EffectResolutionService clears this and re-invokes this method once the resolution ends.
        if (gameData.deferPlayerLossCheck) {
            return false;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            int life = gameData.getLife(playerId);
            int poison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
            if (life <= 0 || poison >= 10) {
                // Check if the player is protected from losing (e.g. Platinum Angel)
                if (!gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                    continue;
                }

                // Check if ALL active loss conditions are individually prevented (e.g. Phyrexian Unlife)
                boolean loseFromLife = life <= 0 && gameQueryService.canPlayerLoseFromLife(gameData, playerId);
                boolean loseFromPoison = poison >= 10;
                if (!loseFromLife && !loseFromPoison) {
                    continue;
                }

                // Lich's Mirror: replace the loss with a full reset instead of finishing the game.
                if (replaceLossWithGameReset(gameData, playerId)) {
                    continue;
                }

                // "Whenever a player loses the game" triggers (e.g. Withengar Unbound).
                firePlayerLosesGameTriggers(gameData, playerId);

                gameData.status = GameStatus.FINISHED;

                // During MCTS simulation, only set the status — skip all external side effects
                if (gameData.simulation) {
                    return true;
                }

                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);

                String logEntry;
                if (poison >= 10) {
                    logEntry = gameData.playerIdToName.get(playerId) + " has 10 poison counters and loses! " + winnerName + " wins!";
                } else {
                    logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                }
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

                sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

                notifyDraftIfTournamentGame(gameData, winnerId);

                gameTimeoutService.onGameFinished(gameData);
                gameRegistry.remove(gameData.id);

                log.info("Game {} - {} wins! {} is at {} life, {} poison", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life, poison);
                return true;
            }
        }
        return false;
    }

    public void declareWinner(GameData gameData, UUID winnerId) {
        // "Whenever a player loses the game" triggers (e.g. Withengar Unbound).
        // In 2-player the loser is the winner's opponent.
        firePlayerLosesGameTriggers(gameData, gameQueryService.getOpponentId(gameData, winnerId));

        gameData.status = GameStatus.FINISHED;

        if (gameData.simulation) return;

        String winnerName = gameData.playerIdToName.get(winnerId);

        String logEntry = winnerName + " wins the game!";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

        notifyDraftIfTournamentGame(gameData, winnerId);

        gameTimeoutService.onGameFinished(gameData);
        gameRegistry.remove(gameData.id);

        log.info("Game {} - {} wins!", gameData.id, winnerName);
    }

    /**
     * All remaining players lose simultaneously (CR 104.4a) — e.g. Triskaidekaphobia when every
     * player has exactly 13 life. Ends the game with no winner ({@code GameOverMessage} nulls).
     */
    public void declareDraw(GameData gameData) {
        gameData.status = GameStatus.FINISHED;

        if (gameData.simulation) return;

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text("The game is a draw."));
        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(null, null));

        gameTimeoutService.onGameFinished(gameData);
        gameRegistry.remove(gameData.id);

        log.info("Game {} - draw", gameData.id);
    }

    /**
     * Puts {@link EffectSlot#ON_PLAYER_LOSES_GAME} triggers (e.g. Withengar Unbound's
     * "Whenever a player loses the game, put thirteen +1/+1 counters on it") onto the stack
     * for every permanent on the battlefield that has such a trigger.
     *
     * <p>This engine is strictly 2-player and the game ends the instant a player loses, so
     * these triggers go onto the stack but the game finishes before they can resolve. The
     * wiring is kept so the ability is modeled correctly should multiplayer ever exist.
     */
    private void firePlayerLosesGameTriggers(GameData gameData, UUID losingPlayerId) {
        if (gameData.simulation || losingPlayerId == null) {
            return;
        }

        gameData.forEachPermanent((controllerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_PLAYER_LOSES_GAME);
            if (effects.isEmpty()) {
                return;
            }

            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId());
            se.setNonTargeting(true);
            gameData.stack.add(se);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), "'s triggered ability goes on the stack ("
                    + gameData.playerIdToName.get(losingPlayerId) + " loses the game)."));
        });
    }

    private void notifyDraftIfTournamentGame(GameData gameData, UUID winnerId) {
        if (gameData.draftId != null) {
            DraftData draftData = draftRegistry.get(gameData.draftId);
            if (draftData != null) {
                TournamentResultHandler handler = tournamentResultHandler.getIfAvailable();
                if (handler != null) {
                    handler.handleGameFinished(draftData, winnerId);
                }
            }
        }
    }
}
