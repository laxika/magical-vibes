package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LifeGainOpponentLifeLossWatcher;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterLifeGainOpponentLifeLossThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Registers Vizkopa Guildmage's turn-scoped delayed trigger: for the rest of the turn, whenever the
 * controller gains life, each opponent loses that much life. Each resolution adds its own watcher so
 * repeated activations stack; {@code TriggerCollectionService} fires the triggers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterLifeGainOpponentLifeLossThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterLifeGainOpponentLifeLossThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.lifeGainOpponentLifeLossWatchers.add(
                new LifeGainOpponentLifeLossWatcher(entry.getControllerId(), entry.getCard()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": whenever you gain life this turn, each opponent loses that much life."));
        log.info("Game {} - {} registers life-gain drain for the turn",
                gameData.id, entry.getCard().getName());
    }
}
