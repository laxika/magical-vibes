package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawDamageReplacementEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Registers Words of War's one-shot replacement of the controller's next draw this turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterNextDrawDamageReplacementEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterNextDrawDamageReplacementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        if (controllerId == null || targetId == null || entry.getCard() == null) {
            return;
        }

        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), new RegisterNextDrawDamageReplacementEffect(entry.getCard(), targetId), controllerId,
                Duration.UNTIL_END_OF_TURN));

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text("The next time " + playerName + " would draw a card this turn, Words of War will deal 2 damage to the chosen target instead (")
                .card(entry.getCard())
                .text(").")
                .build());
        log.info("Game {} - {} registers Words of War's next-draw replacement",
                gameData.id, playerName);
    }
}
