package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseOrStopContinuationEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.FlipUntilLoseOrStopSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the controller's choice to stop or flip again after a won flip. */
@Component
@RequiredArgsConstructor
public class FlipUntilLoseOrStopContinuationHandler implements MayEffectHandlerBean {

    private final FlipUntilLoseOrStopSupport support;
    private final InputCompletionService inputCompletionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipUntilLoseOrStopContinuationEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        FlipUntilLoseOrStopContinuationEffect continuation =
                (FlipUntilLoseOrStopContinuationEffect) ability.effects().getFirst();

        if (!accepted) {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " stops flipping for "
                    + ability.sourceCard().getName() + "."));
            support.queueRewards(gameData, continuation.wins(), continuation.rewards());
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (support.flip(gameData, ability.controllerId(), ability.sourceCard().getName())) {
            support.queueContinue(gameData, ability.sourceCard(), ability.controllerId(), ability.targetCardId(),
                    continuation.wins() + 1, continuation.rewards());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
