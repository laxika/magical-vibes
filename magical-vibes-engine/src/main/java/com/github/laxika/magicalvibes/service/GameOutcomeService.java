package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.outcome.LossOutcome;
import com.github.laxika.magicalvibes.service.outcome.LossReason;
import com.github.laxika.magicalvibes.service.outcome.LossReplacer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class GameOutcomeService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final List<LossReplacer> lossReplacers;
    private final GameMutationCoordinator mutationCoordinator;

    public GameOutcomeService(GameQueryService gameQueryService,
                              GameLogService gameLogService,
                              @Lazy List<LossReplacer> lossReplacers,
                              GameMutationCoordinator mutationCoordinator) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        // @Lazy: a replacer reaches deep into the engine (removal, life, graveyard) and those
        // paths lead back here, so the list resolves on first use rather than at construction.
        this.lossReplacers = lossReplacers;
        this.mutationCoordinator = mutationCoordinator;
    }

    /**
     * The single gate every game loss goes through (CR 104.3).
     *
     * <p>Any code about to finish the game because a player lost must ask this first and act on
     * the answer — only {@link LossOutcome#LOSES} means the loss actually happens. Running the
     * chain in one place is the point: it applies the blanket "can't lose" effects, then the
     * prevention that only {@code reason} allows, then every registered {@link LossReplacer}, in
     * that order. Hand-rolling any part of it is how a call site ends up honoring Platinum Angel
     * but silently ignoring Lich's Mirror.
     *
     * <p>This method never logs and never finishes the game. Callers word their own message
     * (they each have a different one) and own the win/draw decision that follows.
     */
    public LossOutcome resolveLoss(GameData gameData, UUID losingPlayerId, LossReason reason) {
        if (losingPlayerId == null || !gameData.playerIds.contains(losingPlayerId)) {
            return LossOutcome.PREVENTED;
        }
        if (!gameQueryService.canPlayerLoseGame(gameData, losingPlayerId)) {
            return LossOutcome.PREVENTED;
        }
        // Phyrexian Unlife stops only the 0-or-less-life loss; poison and everything else land.
        if (reason == LossReason.LIFE && !gameQueryService.canPlayerLoseFromLife(gameData, losingPlayerId)) {
            return LossOutcome.PREVENTED;
        }
        for (LossReplacer replacer : lossReplacers) {
            if (replacer.tryReplace(gameData, losingPlayerId, reason)) {
                return LossOutcome.REPLACED;
            }
        }
        return LossOutcome.LOSES;
    }

    /**
     * Whether {@code winnerId} may win outright from a "you win the game" effect.
     *
     * <p>Deliberately NOT routed through {@link #resolveLoss}: a win effect ends the game
     * immediately instead of making the opponent lose, so there is no loss event for a
     * {@link LossReplacer} to replace. Per the Lich's Mirror ruling, "Lich's Mirror has no effect
     * if a spell or ability (such as the one from Helix Pinnacle) states that a player 'wins the
     * game.' If a player wins the game, the game ends immediately."
     *
     * <p>The check covers both directions of game-outcome restrictions: an opponent's
     * {@code CantLoseGameEffect} and the winner's or an opponent's {@code CantWinGameEffect}.
     */
    public boolean canPlayerWinGame(GameData gameData, UUID winnerId) {
        return !gameQueryService.playerHasCantWinGameEffect(gameData, winnerId)
                && gameQueryService.canPlayerLoseGame(gameData, gameQueryService.getOpponentId(gameData, winnerId));
    }

    public boolean checkWinCondition(GameData gameData) {
        if (gameData.gameResult != null) {
            return true;
        }
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
                // Poison is checked first: a player at 0 life AND 10 poison still loses even with
                // Phyrexian Unlife, because that only prevents the life half (CR 704.5a/704.5c).
                LossReason reason = poison >= 10 ? LossReason.POISON : LossReason.LIFE;
                if (resolveLoss(gameData, playerId, reason) != LossOutcome.LOSES) {
                    continue;
                }

                // "Whenever a player loses the game" triggers (e.g. Withengar Unbound).
                firePlayerLosesGameTriggers(gameData, playerId);

                // During MCTS simulation, only set the status — skip all external side effects
                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);
                if (gameData.simulation) {
                    finish(gameData, GameEventFact.GameResult.WIN, winnerId,
                            GameEventAudience.allPlayers());
                    return true;
                }

                String logEntry;
                if (poison >= 10) {
                    logEntry = gameData.playerIdToName.get(playerId) + " has 10 poison counters and loses! " + winnerName + " wins!";
                } else {
                    logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                }
                gameLogService.append(gameData, GameLog.text(logEntry));

                finish(gameData, GameEventFact.GameResult.WIN, winnerId,
                        GameEventAudience.allPlayers());

                log.info("Game {} - {} wins! {} is at {} life, {} poison", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life, poison);
                return true;
            }
        }
        return false;
    }

    public void declareWinner(GameData gameData, UUID winnerId) {
        if (gameData.gameResult != null) {
            return;
        }
        // "Whenever a player loses the game" triggers (e.g. Withengar Unbound).
        // In 2-player the loser is the winner's opponent.
        firePlayerLosesGameTriggers(gameData, gameQueryService.getOpponentId(gameData, winnerId));
        if (gameData.simulation) {
            finish(gameData, GameEventFact.GameResult.WIN, winnerId,
                    GameEventAudience.allPlayers());
            return;
        }

        String winnerName = gameData.playerIdToName.get(winnerId);

        String logEntry = winnerName + " wins the game!";
        gameLogService.append(gameData, GameLog.text(logEntry));

        finish(gameData, GameEventFact.GameResult.WIN, winnerId,
                GameEventAudience.allPlayers());

        log.info("Game {} - {} wins!", gameData.id, winnerName);
    }

    /**
     * All remaining players lose simultaneously (CR 104.4a) — e.g. Triskaidekaphobia when every
     * player has exactly 13 life. Ends the game with no winner ({@code GameOverMessage} nulls).
     */
    public void declareDraw(GameData gameData) {
        if (gameData.gameResult != null) {
            return;
        }
        if (gameData.simulation) {
            finish(gameData, GameEventFact.GameResult.DRAW, null,
                    GameEventAudience.allPlayers());
            return;
        }

        gameLogService.append(gameData, GameLog.text("The game is a draw."));
        finish(gameData, GameEventFact.GameResult.DRAW, null,
                GameEventAudience.allPlayers());

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

            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s triggered ability goes on the stack ("
                    + gameData.playerIdToName.get(losingPlayerId) + " loses the game)."));
        });
    }

    /**
     * Closes a runtime game without a rules result or outbound game-over message.
     */
    public void abandon(GameData gameData) {
        finish(gameData, GameEventFact.GameResult.ABANDONED, null,
                GameEventAudience.internalOnly());
    }

    private void finish(
            GameData gameData,
            GameEventFact.GameResult result,
            UUID winnerId,
            GameEventAudience audience) {
        if (gameData.gameResult != null) {
            return;
        }
        gameData.gameResult = result;
        gameData.winnerPlayerId = winnerId;
        gameData.status = GameStatus.FINISHED;

        if (!gameData.simulation) {
            mutationCoordinator.emit(gameData,
                    new GameEventFact.GameEnded(result, winnerId),
                    audience);
        }
    }
}
