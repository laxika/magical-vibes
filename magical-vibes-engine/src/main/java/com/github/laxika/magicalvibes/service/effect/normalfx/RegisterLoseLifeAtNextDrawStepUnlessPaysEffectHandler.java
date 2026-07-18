package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.LoseLifeAtNextDrawStepUnlessPays;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseLifeAtNextDrawStepUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Nafs Asp's {@code ON_DAMAGE_TO_PLAYER} trigger: reads the damaged player from the stack
 * entry's {@code targetId} (baked in as {@code DAMAGED_PLAYER}) and queues a
 * {@link LoseLifeAtNextDrawStepUnlessPays} delayed action, drained at that player's next draw step.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterLoseLifeAtNextDrawStepUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterLoseLifeAtNextDrawStepUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterLoseLifeAtNextDrawStepUnlessPaysEffect) effect;
        UUID playerId = entry.getTargetId();
        if (playerId == null) return;

        gameData.queueDelayedAction(new LoseLifeAtNextDrawStepUnlessPays(
                playerId, e.lifeLoss(), e.payAmount(), entry.getCard()));

        String playerName = gameData.playerIdToName.get(playerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " must pay {" + e.payAmount() + "} before their next draw step or lose " + e.lifeLoss() + " life. (", entry.getCard(), ")"));
        log.info("Game {} - {} scheduled a draw-step pay-or-lose-{}-life obligation on {}",
                gameData.id, entry.getCard().getName(), e.lifeLoss(), playerName);
    }
}
