package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedSourceDamageMultiplication;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MultiplyCombatDamageFromTargetCreatureToOpponentsUntilNextTurnEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        if (target == null) {
            return;
        }

        gameData.queueDelayedAction(new DelayedSourceDamageMultiplication(
                target.getId(), entry.getControllerId(), e.multiplier()));
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text("'s combat damage to your opponents is multiplied by ")
                .text(String.valueOf(e.multiplier()))
                .text(" until your next turn.")
                .build());
    }
}
