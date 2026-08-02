package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.OpponentGraveyardLifeLossWatcher;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOpponentGraveyardLifeLossThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Registers Duskmantle Guildmage's turn-scoped delayed trigger: for the rest of the turn, whenever a
 * card is put into an opponent's graveyard from anywhere, that player loses 1 life. Each resolution
 * adds its own watcher so repeated activations stack; {@code GraveyardService} fires the triggers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterOpponentGraveyardLifeLossThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterOpponentGraveyardLifeLossThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.opponentGraveyardLifeLossWatchers.add(
                new OpponentGraveyardLifeLossWatcher(entry.getControllerId(), entry.getCard()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": whenever a card is put into an opponent's graveyard this turn, that player loses 1 life."));
        log.info("Game {} - {} registers opponent graveyard life loss for the turn",
                gameData.id, entry.getCard().getName());
    }
}
