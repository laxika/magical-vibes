package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostTargetAndSharingCreaturesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetAndSharingCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostTargetAndSharingCreaturesUntilEndOfTurnEffect) effect;
        UUID targetId = entry.targetsForEffect(boost).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), context);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), context);
        Set<CardColor> targetColors = gameQueryService.getEffectiveColors(gameData, target);
        int[] affectedCount = {0};

        gameData.forEachPermanent((ignored, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || (!permanent.getId().equals(targetId)
                    && (targetColors.isEmpty()
                    || gameQueryService.getEffectiveColors(gameData, permanent).stream()
                    .noneMatch(targetColors::contains)))) {
                return;
            }
            permanent.setPowerModifier(permanent.getPowerModifier() + powerBoost);
            permanent.setToughnessModifier(permanent.getToughnessModifier() + toughnessBoost);
            affectedCount[0]++;
        });

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" gives %+d/%+d to %d creature(s) until end of turn.",
                        powerBoost, toughnessBoost, affectedCount[0]))
                .build());
        log.info("Game {} - {} gives {}/{} to {} color-sharing creatures", gameData.id,
                entry.getCard().getName(), powerBoost, toughnessBoost, affectedCount[0]);
    }
}
