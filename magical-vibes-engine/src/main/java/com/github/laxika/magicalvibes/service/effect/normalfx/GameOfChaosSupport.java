package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.GameOfChaosFlipAgainEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared coin-flip / life-swap helpers for Game of Chaos, used by both the spell handler (first
 * flip) and the "flip again" may-ability handler (subsequent flips).
 */
@Component
@RequiredArgsConstructor
public class GameOfChaosSupport {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;

    /**
     * Performs one Game of Chaos coin flip from the spell controller's perspective at the given life
     * {@code stake}, applies the life swap, logs it, and checks for a game-ending life total.
     *
     * <p>The flip's result is kept in {@link GameData#gameOfChaosFlipControllerWins} until the life changes are made.
     * Before them, the Lich's Duel Mastery sacrifice the loser's loss would cause is asked about (CR 616.1); when that
     * stops the caller, it is resolved or answered again and uses the kept result instead of flipping again.
     *
     * @return the player who decides whether to flip again (the controller on a win, the opponent on
     *         a loss), or {@code null} if the game ended as a result of this flip.
     */
    public UUID flipRound(GameData gameData, String sourceName, UUID controllerId, UUID opponentId, int stake) {
        if (gameData.gameOfChaosFlipControllerWins == null) {
            gameData.gameOfChaosFlipControllerWins = flip(gameData, sourceName, controllerId, stake);
        }
        boolean controllerWins = gameData.gameOfChaosFlipControllerWins;
        lifeSupport.askBeforeLifeLosses(gameData, Map.of(controllerWins ? opponentId : controllerId, stake));
        gameData.gameOfChaosFlipControllerWins = null;

        UUID decidingPlayer;
        if (controllerWins) {
            lifeSupport.applyGainLife(gameData, controllerId, stake, sourceName);
            lifeSupport.applyLifeLoss(gameData, opponentId, stake, sourceName);
            decidingPlayer = controllerId;
        } else {
            lifeSupport.applyLifeLoss(gameData, controllerId, stake, sourceName);
            lifeSupport.applyGainLife(gameData, opponentId, stake, sourceName);
            decidingPlayer = opponentId;
        }

        gameOutcomeService.checkWinCondition(gameData);
        if (gameData.status == GameStatus.FINISHED) {
            return null;
        }
        return decidingPlayer;
    }

    /** Flips the coin, logs the result and checks the coin flip triggers; {@code true} when the controller wins. */
    private boolean flip(GameData gameData, String sourceName, UUID controllerId, int stake) {
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
        boolean controllerWins = result.heads();
        String controllerName = gameData.playerIdToName.get(controllerId);

        gameLogService.append(gameData, GameLog.text(sourceName + ": " + controllerName
                + (controllerWins ? " wins" : " loses") + " the flip (stakes " + stake + ")"
                + coinFlipService.replacementDetails(result) + "."));

        if (controllerWins && result.isActualCoinFlip()) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
        } else if (result.isActualCoinFlip()) {
            triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, controllerId);
        }
        return controllerWins;
    }

    /** Queues the repeating "flip again?" prompt for the deciding player with the doubled stake. */
    public void queueFlipAgain(GameData gameData, Card sourceCard, UUID controllerId, UUID opponentId,
                               UUID decidingPlayer, int nextStake) {
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                sourceCard,
                decidingPlayer,
                List.of(new GameOfChaosFlipAgainEffect(controllerId, opponentId, nextStake)),
                sourceCard.getName() + " - Flip again? (stakes " + nextStake + ")"));
    }
}
