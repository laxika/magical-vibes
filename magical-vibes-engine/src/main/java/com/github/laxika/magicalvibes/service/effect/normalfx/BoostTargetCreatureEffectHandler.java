package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
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
    private final SubjectResolutionService subjectResolutionService;

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

        if (!(boost.subject() instanceof PermanentSubject.Target)) {
            // The target and the creatures named with it, found once as the effect resolves (CR 611.2c).
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, boost.subject())) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    applyBoost(gameData, entry, permanent, powerBoost, toughnessBoost, boost.duration());
                }
            }
            return;
        }

        // Multi-target: apply boost to each valid target of this effect's target group
        // (the whole flat list when the effect isn't bound to a group).
        List<UUID> targetIds = boost.targetGroup() >= 0
                ? entry.targetsForGroup(boost.targetGroup())
                : entry.targetsForEffect(effect);
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

        if (boost.targetGroup() >= 0) {
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
                            int powerBoost, int toughnessBoost, Duration duration) {
        // A layer-7c floating effect on the target with the stated duration (CR 613.4c, 611.2a): until end of turn,
        // until end of combat, until the ability controller's next turn, or, with no stated duration, indefinitely.
        gameData.addPowerToughnessBoost(FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(),
                entry.getControllerId(), target.getId(), powerBoost, toughnessBoost,
                Duration.UNTIL_YOUR_NEXT_TURN.equals(duration) || Duration.UNTIL_END_OF_COMBAT.equals(duration)
                        || Duration.PERMANENT.equals(duration)
                        ? duration : Duration.UNTIL_END_OF_TURN);

        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(String.format(" gets %+d/%+d %s.", powerBoost, toughnessBoost,
                        Duration.UNTIL_YOUR_NEXT_TURN.equals(duration) ? "until your next turn"
                                : Duration.PERMANENT.equals(duration) ? "indefinitely" : "until end of turn"))
                .build());

        log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), powerBoost, toughnessBoost);
    }
}
