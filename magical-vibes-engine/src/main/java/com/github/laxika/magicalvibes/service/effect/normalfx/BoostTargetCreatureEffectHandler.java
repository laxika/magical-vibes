package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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
public class BoostTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
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
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
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
                applyBoost(gameData, entry, target, powerBoost, toughnessBoost, boost.duration());
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        applyBoost(gameData, entry, target, powerBoost, toughnessBoost, boost.duration());
    }

    void resolveForTarget(GameData gameData, StackEntry entry, Permanent target,
                          BoostTargetCreatureEffect boost) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);
        applyBoost(gameData, entry, target, powerBoost, toughnessBoost, boost.duration());
    }

    private void applyBoost(GameData gameData, StackEntry entry, Permanent target,
                            int powerBoost, int toughnessBoost, GrantDuration duration) {
        // The end-of-turn pump is the plain modifier, wiped by turn cleanup. An "until your next
        // turn" pump must outlive that cleanup and end at the *ability controller's* next turn, so
        // it is recorded as a floating continuous effect keyed to that controller instead — the
        // layered pass reads the sublayer-7c addition off it (same read path as Riding the Dilu
        // Horse's indefinite buff).
        if (duration == GrantDuration.UNTIL_YOUR_NEXT_TURN) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(),
                    new BuffTargetCreatureIndefinitelyEffect(powerBoost, toughnessBoost),
                    target.getId(), null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        } else {
            target.setPowerModifier(target.getPowerModifier() + powerBoost);
            target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(String.format(" gets %+d/%+d %s.", powerBoost, toughnessBoost,
                        duration == GrantDuration.UNTIL_YOUR_NEXT_TURN
                                ? "until your next turn" : "until end of turn"))
                .build());

        log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), powerBoost, toughnessBoost);
    }
}
