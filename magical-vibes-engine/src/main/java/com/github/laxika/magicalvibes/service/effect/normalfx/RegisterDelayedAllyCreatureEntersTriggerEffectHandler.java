package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureEntersTriggerWatcher;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAllyCreatureEntersTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Registers a turn-scoped trigger for each creature entering under the controller's control. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedAllyCreatureEntersTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedAllyCreatureEntersTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedAllyCreatureEntersTriggerEffect delayed =
                (RegisterDelayedAllyCreatureEntersTriggerEffect) effect;
        gameData.allyCreatureEntersTriggerWatchers.add(
                new CreatureEntersTriggerWatcher(entry.getControllerId(), entry.getCard(), delayed.effect()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": whenever a creature you control enters this turn, its delayed ability triggers."));
        log.info("Game {} - {} registers an ally-creature-entry trigger for the turn",
                gameData.id, entry.getCard().getName());
    }
}
