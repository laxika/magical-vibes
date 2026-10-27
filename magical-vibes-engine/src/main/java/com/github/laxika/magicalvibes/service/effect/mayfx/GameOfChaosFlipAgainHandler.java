package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GameOfChaosFlipAgainEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GameOfChaosSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
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
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;
    private final CoinFlipService coinFlipService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GameOfChaosFlipAgainEffect.class;
    }

    /**
     * An accepted answer flips and swaps life. The flip's result is kept until the swap
     * ({@link GameOfChaosSupport#flipRound}), so the loser's Lich's Duel Mastery sacrifice is asked about (CR 616.1)
     * before any life changes and the same answer is completed again without flipping again. The flip is the first
     * thing an accepted answer does, so a Krark's Thumb flip whose results disagree stops it until the spell's
     * controller chooses the kept result, and the answer is then completed again with it.
     */
    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Runnable completion = () -> permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                gameData, player, ability, accepted, () -> answer(gameData, player, accepted, ability));
        if (accepted) {
            coinFlipService.answerChoosingResultsFirst(gameData, completion,
                    new GameData.MayAnswerReplay(player, ability, true));
        } else {
            completion.run();
        }
    }

    private void answer(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        GameOfChaosFlipAgainEffect e = (GameOfChaosFlipAgainEffect) ability.effects().getFirst();
        String sourceName = ability.sourceCard().getName();

        if (!accepted) {
            gameLogService.append(gameData, GameLog.text(
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
