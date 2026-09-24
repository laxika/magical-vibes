package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DamagedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDamagedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Registers a turn-scoped trigger for creatures damaged by the registering source. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedDamagedCreatureDeathTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedDamagedCreatureDeathTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RegisterDelayedDamagedCreatureDeathTriggerEffect delayed =
                (RegisterDelayedDamagedCreatureDeathTriggerEffect) effect;
        gameData.damagedCreatureDeathTriggerWatchers.add(new DamagedCreatureDeathTriggerWatcher(
                entry.getCard().getId(), entry.getControllerId(), entry.getCard(), delayed.effect()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": whenever a creature you control dealt damage this way dies this turn, its delayed ability triggers."));
        log.info("Game {} - {} registers a damaged-creature death trigger for the turn",
                gameData.id, entry.getCard().getName());
    }
}
