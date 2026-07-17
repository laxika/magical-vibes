package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.GameOfChaosFlipAgainEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    /**
     * Performs one Game of Chaos coin flip from the spell controller's perspective at the given life
     * {@code stake}, applies the life swap, logs it, and checks for a game-ending life total.
     *
     * @return the player who decides whether to flip again (the controller on a win, the opponent on
     *         a loss), or {@code null} if the game ended as a result of this flip.
     */
    public UUID flipRound(GameData gameData, String sourceName, UUID controllerId, UUID opponentId, int stake) {
        boolean controllerWins = ThreadLocalRandom.current().nextBoolean();
        String controllerName = gameData.playerIdToName.get(controllerId);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + ": " + controllerName
                + (controllerWins ? " wins" : " loses") + " the flip (stakes " + stake + ")."));

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
