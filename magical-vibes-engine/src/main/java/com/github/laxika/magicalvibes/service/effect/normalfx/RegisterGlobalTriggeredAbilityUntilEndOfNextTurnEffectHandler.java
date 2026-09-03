package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Stores a global trigger through the end of its controller's next turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect) effect;
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                entry.getControllerId(), entry.getCard(), registration.slot(), registration.triggeredEffect(),
                registration.targetFilter(), true, gameData.turnNumber));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a global triggered ability until the end of their next turn."));
        log.info("Game {} - {} registers a global {} trigger until the end of their next turn",
                gameData.id, entry.getCard().getName(), registration.slot().name());
    }
}
