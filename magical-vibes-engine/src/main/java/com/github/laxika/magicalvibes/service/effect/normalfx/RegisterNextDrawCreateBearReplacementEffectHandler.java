package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawCreateBearReplacementEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Registers Words of Wilding's one-shot replacement of the controller's next draw this turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterNextDrawCreateBearReplacementEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterNextDrawCreateBearReplacementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String sourceSetCode = entry.getCard() == null ? null : entry.getCard().getSetCode();
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), new RegisterNextDrawCreateBearReplacementEffect(sourceSetCode), controllerId,
                Duration.UNTIL_END_OF_TURN));

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text("The next time " + playerName + " would draw a card this turn, they'll create a 2/2 green Bear creature token instead (")
                .card(entry.getCard())
                .text(").")
                .build());
        log.info("Game {} - {} registers Words of Wilding's next-draw replacement",
                gameData.id, playerName);
    }
}
