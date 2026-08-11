package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Registers a turn-scoped trigger for each creature that dies. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCreatureDeathTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCreatureDeathTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedCreatureDeathTriggerEffect delayed = (RegisterDelayedCreatureDeathTriggerEffect) effect;
        gameData.creatureDeathTriggerWatchers.add(
                new CreatureDeathTriggerWatcher(entry.getControllerId(), entry.getCard(), delayed.effect()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": whenever a creature dies this turn, its delayed ability triggers."));
        log.info("Game {} - {} registers a creature-death trigger for the turn",
                gameData.id, entry.getCard().getName());
    }
}
