package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GameOfChaosFlipAgainEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GameOfChaosSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles the deciding player's answer to Game of Chaos's "flip again?" prompt. On accept, performs
 * the next coin flip at the carried (doubled) stake and re-queues the prompt for the new winner; on
 * decline (or once the game ends), the loop stops.
 */
@Component
@RequiredArgsConstructor
public class GameOfChaosFlipAgainHandler implements MayEffectHandlerBean {

    private final GameOfChaosSupport gameOfChaosSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GameOfChaosFlipAgainEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        GameOfChaosFlipAgainEffect e = (GameOfChaosFlipAgainEffect) ability.effects().getFirst();
        String sourceName = ability.sourceCard().getName();

        if (!accepted) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    player.getUsername() + " declines to flip again (" + sourceName + ")."));
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID decidingPlayer = gameOfChaosSupport.flipRound(gameData, sourceName,
                e.spellControllerId(), e.opponentId(), e.stake());
        if (decidingPlayer != null) {
            gameOfChaosSupport.queueFlipAgain(gameData, ability.sourceCard(),
                    e.spellControllerId(), e.opponentId(), decidingPlayer, e.stake() * 2);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
