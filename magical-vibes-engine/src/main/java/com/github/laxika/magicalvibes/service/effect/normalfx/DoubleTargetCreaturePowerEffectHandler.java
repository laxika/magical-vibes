package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link DoubleTargetCreaturePowerEffect}: each creature its subject names (the target, or a set found once
 * as it resolves) gets +X/+0 until end of turn for each doubling, X being its power as that doubling is applied; under
 * Maro's Gone Nuts each doubling multiplies by {@code 2 * doublingFactor} ({@link MaroGoneNutsSupport#doublingIncrease}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleTargetCreaturePowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleTargetCreaturePowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var doubling = (DoubleTargetCreaturePowerEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int times = Math.max(0, amountEvaluationService.evaluate(gameData, doubling.times(),
                AmountContext.forStackEntry(entry, source)));

        for (Permanent creature : creatures(gameData, entry, doubling)) {
            for (int i = 0; i < times; i++) {
                int currentPower = gameQueryService.getEffectivePower(gameData, creature);
                // Doubling 0 power or a saturated power changes nothing, so a huge X needn't loop on.
                if (currentPower == 0) {
                    break;
                }
                int factor = MaroGoneNutsSupport.apply(gameData, effect, 2) - 1;
                gameData.addPowerToughnessBoost(entry, creature.getId(),
                        SaturatingMath.multiply(currentPower, factor), 0);
                if (gameQueryService.getEffectivePower(gameData, creature) == currentPower) {
                    break;
                }
            }

            gameLogService.append(gameData, GameLog.builder()
                    .card(creature.getCard())
                    .text("'s power is doubled " + times + " time(s) until end of turn.")
                    .build());
            log.info("Game {} - {} power doubled {} time(s)", gameData.id, creature.getCard().getName(), times);
        }
    }

    /** The creatures doubled: the entry's target, or the creatures the subject names as the effect resolves. */
    private List<Permanent> creatures(GameData gameData, StackEntry entry, DoubleTargetCreaturePowerEffect doubling) {
        if (!(doubling.subject() instanceof PermanentSubject.Target)) {
            return subjectResolutionService.permanents(gameData, entry, doubling, doubling.subject()).stream()
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                    .toList();
        }
        List<UUID> targetIds = entry.targetsForEffect(doubling);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        List<Permanent> targets = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                targets.add(target);
            }
        }
        return targets;
    }
}
