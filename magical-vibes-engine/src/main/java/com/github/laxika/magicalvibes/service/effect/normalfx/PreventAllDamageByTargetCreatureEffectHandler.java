package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageByTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreventAllDamageByTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventAllDamageByTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();
        if ((targetIds == null || targetIds.isEmpty()) && entry.getTargetId() != null) {
            // Single-target activated ability path (e.g. Resistance Fighter) stores the target
            // in the scalar targetId rather than the flat targetIds list.
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds == null || targetIds.isEmpty()) return;

        boolean combatOnly = ((PreventAllDamageByTargetCreatureEffect) effect).combatOnly();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;

            if (combatOnly) {
                gameData.creaturesPreventedFromDealingCombatDamage.add(targetId);
            } else {
                gameData.permanentsPreventedFromDealingDamage.add(targetId);
            }
            String logEntry = "All " + (combatOnly ? "combat damage " : "damage ")
                    + target.getCard().getName() + " would deal this turn is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} prevented from dealing {}damage this turn",
                    gameData.id, target.getCard().getName(), combatOnly ? "combat " : "");
        }
    }
}
