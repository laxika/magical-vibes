package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TemporaryGlobalTriggerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Stores a global trigger through the end of its controller's next turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect) effect;
        CardEffect triggeredEffect = registration.triggeredEffect();
        UUID expirationPlayerId = entry.getControllerId();
        if (triggeredEffect instanceof TemporaryGlobalTriggerEffect temporary) {
            UUID targetPlayerId = resolveTargetPlayerId(gameData, entry);
            triggeredEffect = temporary.bindTo(entry.getSourcePermanentId(), targetPlayerId);
            if (targetPlayerId != null) {
                expirationPlayerId = targetPlayerId;
            }
        }
        gameData.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                entry.getControllerId(), entry.getCard(), registration.slot(), triggeredEffect,
                registration.targetFilter(), true, false, gameData.turnNumber, expirationPlayerId));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a global triggered ability until the end of their next turn."));
        log.info("Game {} - {} registers a global {} trigger until the end of their next turn",
                gameData.id, entry.getCard().getName(), registration.slot().name());
    }

    private UUID resolveTargetPlayerId(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getAttackedTargetId() != null
                ? entry.getAttackedTargetId() : entry.getTargetId();
        if (targetId == null) {
            return null;
        }
        if (gameData.playerIds.contains(targetId)) {
            return targetId;
        }
        return gameQueryService.findPermanentController(gameData, targetId);
    }
}
