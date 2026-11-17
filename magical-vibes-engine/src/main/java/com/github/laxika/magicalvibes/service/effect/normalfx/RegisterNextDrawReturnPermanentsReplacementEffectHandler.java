package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawReturnPermanentsReplacementEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Registers Words of Wind's one-shot replacement of the controller's next draw this turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterNextDrawReturnPermanentsReplacementEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterNextDrawReturnPermanentsReplacementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), effect, controllerId,
                Duration.UNTIL_END_OF_TURN));

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text("The next time " + playerName + " would draw a card this turn, each player will return a permanent they control to its owner's hand instead (")
                .card(entry.getCard())
                .text(").")
                .build());
        log.info("Game {} - {} registers Words of Wind's next-draw replacement",
                gameData.id, playerName);
    }
}
