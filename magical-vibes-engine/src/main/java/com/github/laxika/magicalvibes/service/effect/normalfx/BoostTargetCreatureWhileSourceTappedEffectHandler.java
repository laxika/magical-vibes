package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link BoostTargetCreatureWhileSourceTappedEffect}: records a {@code WHILE_SOURCE_TAPPED}
 * floating continuous effect on the target that grants the +P/+T boost for as long as the source
 * permanent remains tapped. The wrapped {@link BuffTargetCreatureIndefinitelyEffect} is read by the
 * CR 613 layered pass in sublayer 7c. The effect is expired when the source becomes untapped
 * ({@code CreatureControlService.onSourceUntapped}) or leaves the battlefield
 * ({@code GameData.expireFloatingEffectsForDepartedSource}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoostTargetCreatureWhileSourceTappedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreatureWhileSourceTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostTargetCreatureWhileSourceTappedEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue; // Partially resolves — skip removed targets
            }

            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), sourcePermanentId, entry.getControllerId(),
                    new BuffTargetCreatureIndefinitelyEffect(boost.power(), boost.toughness()),
                    target.getId(), null, null, EffectDuration.WHILE_SOURCE_TAPPED, 0));

            String logEntry = String.format("%s gets %+d/%+d for as long as %s remains tapped.",
                    target.getCard().getName(), boost.power(), boost.toughness(), entry.getCard().getName());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} gets {}/{} while {} remains tapped", gameData.id,
                    target.getCard().getName(), boost.power(), boost.toughness(), entry.getCard().getName());
        }
    }
}
