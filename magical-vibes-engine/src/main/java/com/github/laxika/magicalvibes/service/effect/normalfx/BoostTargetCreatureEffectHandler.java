package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
public class BoostTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostTargetCreatureEffect) effect;
        // The boost applies to the target, but counting contexts ("you control", "in your
        // graveyard") refer to the effect's controller, so the amount evaluates against the
        // SOURCE permanent (the spell/ability's own permanent), not the target being pumped.
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);

        // Multi-target: apply boost to each valid target of this effect's target group
        // (the whole flat list when the effect isn't bound to a group).
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (!targetIds.isEmpty()) {
            for (UUID targetId : targetIds) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue; // Partially resolves — skip removed targets
                }
                applyBoost(gameData, target, powerBoost, toughnessBoost);
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        applyBoost(gameData, target, powerBoost, toughnessBoost);
    }

    private void applyBoost(GameData gameData, Permanent target, int powerBoost, int toughnessBoost) {
        target.setPowerModifier(target.getPowerModifier() + powerBoost);
        target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);

        String logEntry = String.format("%s gets %+d/%+d until end of turn.",
                target.getCard().getName(), powerBoost, toughnessBoost);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), powerBoost, toughnessBoost);
    }
}
