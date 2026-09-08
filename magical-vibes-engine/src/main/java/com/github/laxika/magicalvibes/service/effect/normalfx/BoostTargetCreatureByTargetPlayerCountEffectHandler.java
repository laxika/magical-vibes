package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureByTargetPlayerCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostTargetCreatureByTargetPlayerCountEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreatureByTargetPlayerCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostTargetCreatureByTargetPlayerCountEffect) effect;
        List<UUID> playerTargets = entry.targetsForGroup(boost.targetPlayerGroup());
        if (playerTargets.isEmpty() || !gameData.playerIds.contains(playerTargets.getFirst())) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext baseContext = AmountContext.forStackEntry(entry, source);
        AmountContext targetPlayerContext = new AmountContext(
                baseContext.controllerId(),
                baseContext.sourcePermanent(),
                playerTargets.getFirst(),
                baseContext.xValue(),
                baseContext.eventValue(),
                baseContext.staticEvaluation(),
                baseContext.chosenPermanentId(),
                baseContext.repeatedAdditionalCosts(),
                baseContext.sourceCard());
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), targetPlayerContext);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), targetPlayerContext);

        for (UUID targetId : entry.targetsForEffect(effect)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            target.setPowerModifier(target.getPowerModifier() + powerBoost);
            target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);
            gameLogService.append(gameData, GameLog.builder()
                    .card(target.getCard())
                    .text(String.format(" gets %+d/%+d until end of turn.", powerBoost, toughnessBoost))
                    .build());
            log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), powerBoost, toughnessBoost);
        }
    }
}
