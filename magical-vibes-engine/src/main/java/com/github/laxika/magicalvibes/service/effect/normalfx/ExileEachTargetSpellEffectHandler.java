package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEachTargetSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves an effect that exiles each still-legal spell in its multi-target group. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileEachTargetSpellEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileEachTargetSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = new ArrayList<>(entry.targetsForEffect(effect));
        for (UUID targetId : targetIds) {
            StackEntry target = gameData.stack.stream()
                    .filter(stackEntry -> stackEntry.getCard().getId().equals(targetId))
                    .findFirst()
                    .orElse(null);
            if (target == null) continue;

            gameData.stack.remove(target);
            stateTriggerService.cleanupResolvedStateTrigger(gameData, target);
            if (target.isCopy()) {
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " (a copy) ceases to exist."));
            } else {
                exileService.exileCard(gameData, target.getControllerId(), target.getCard());
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
            }
            log.info("Game {} - {} exiled from stack by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());
        }
    }
}
