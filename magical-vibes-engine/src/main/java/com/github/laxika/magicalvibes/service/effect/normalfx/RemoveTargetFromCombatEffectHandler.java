package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveTargetFromCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveTargetFromCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (target.isAttacking()) {
            target.setAttacking(false);
            target.setAttackTarget(null);
        }
        if (target.isBlocking()) {
            target.setBlocking(false);
            target.getBlockingTargets().clear();
            target.getBlockingTargetIds().clear();
        }

        String logEntry = entry.getCard().getName() + " removes " + target.getCard().getName() + " from combat.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(), " removes ", target.getCard(), " from combat."));
        log.info("Game {} - {} removes {} from combat", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
