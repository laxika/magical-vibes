package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostPairedCreatureOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostPairedCreatureOfTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostPairedCreatureOfTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostPairedCreatureOfTargetEffect) effect;
        // Counting contexts refer to the effect's controller, so the amounts evaluate against the
        // source, matching BoostTargetCreatureEffect.
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty()) {
            targetIds = entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || target.getPairedWithId() == null) {
                continue;
            }
            Permanent partner = gameQueryService.findPermanentById(gameData, target.getPairedWithId());
            if (partner == null) {
                continue;
            }
            partner.setPowerModifier(partner.getPowerModifier() + powerBoost);
            partner.setToughnessModifier(partner.getToughnessModifier() + toughnessBoost);

            gameLogService.append(gameData, GameLog.builder()
                    .card(partner.getCard())
                    .text(String.format(" gets %+d/%+d until end of turn.", powerBoost, toughnessBoost))
                    .build());

            log.info("Game {} - paired creature {} gets {}/{}", gameData.id, partner.getCard().getName(),
                    powerBoost, toughnessBoost);
        }
    }
}
