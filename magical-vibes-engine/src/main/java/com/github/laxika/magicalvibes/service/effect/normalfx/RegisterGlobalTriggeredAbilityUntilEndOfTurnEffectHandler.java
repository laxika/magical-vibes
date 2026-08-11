package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stores a global trigger registered by a resolving spell until turn cleanup. The trigger remains
 * active if the registering spell has already moved to its owner's graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                entry.getControllerId(), entry.getCard(), registration.slot(), registration.triggeredEffect()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a global triggered ability until end of turn."));
        log.info("Game {} - {} registers a global {} trigger until end of turn",
                gameData.id, entry.getCard().getName(), registration.slot().name());
    }
}
