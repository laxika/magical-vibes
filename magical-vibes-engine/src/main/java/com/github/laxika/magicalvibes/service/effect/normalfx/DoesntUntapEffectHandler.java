package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the stack-based, source-linked {@link DoesntUntapEffect} variants (TARGET scope with a
 * {@code WHILE_SOURCE_*} condition): records an untap-prevention lock on the target permanent that
 * the untap step reads and cleans up once the source leaves the battlefield / is no longer tapped.
 *
 * <p>The {@code SELF}/{@code ENCHANTED} + {@code ALWAYS} variants are continuous static effects read
 * directly in {@code UntapStepService} and never reach this handler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoesntUntapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoesntUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DoesntUntapEffect doesntUntap = (DoesntUntapEffect) effect;

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty()) {
            return;
        }
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        switch (doesntUntap.condition()) {
            case WHILE_SOURCE_ON_BATTLEFIELD -> {
                for (UUID targetId : targetIds) {
                    Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                    if (target == null) {
                        continue;
                    }
                    target.getUntapPreventedWhileSourceOnBattlefieldIds().add(sourcePermanentId);
                    gameLogService.append(gameData, GameLog.cardTextCard(target.getCard(), " won't untap as long as you control ", entry.getCard(), "."));
                    log.info("Game {} - {} untap prevented while {} on battlefield", gameData.id, target.getCard().getName(), entry.getCard().getName());
                }
            }
            case WHILE_SOURCE_TAPPED -> {
                for (UUID targetId : targetIds) {
                    Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                    if (target == null) {
                        continue;
                    }
                    target.getUntapPreventedByPermanentIds().add(sourcePermanentId);
                    gameLogService.append(gameData, GameLog.cardTextCard(target.getCard(), " won't untap as long as ", entry.getCard(), " remains tapped."));
                    log.info("Game {} - {} untap prevented while {} remains tapped", gameData.id, target.getCard().getName(), entry.getCard().getName());
                }
            }
            case ALWAYS -> {
                // SELF/ENCHANTED + ALWAYS are static effects handled by UntapStepService; never resolved on the stack.
            }
        }
    }
}
