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
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreatureWhileSourceTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostTargetCreatureWhileSourceTappedEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.isTapped()) {
            return;
        }

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
                    new BuffTargetCreatureIndefinitelyEffect(boost.power(), boost.toughness(), boost.keywords()),
                    target.getId(), null, null, EffectDuration.WHILE_SOURCE_TAPPED, 0));

            gameLogService.append(gameData, GameLog.builder()
                    .card(target.getCard())
                    .text(describe(boost) + " for as long as ")
                    .card(entry.getCard())
                    .text(" remains tapped.")
                    .build());
            log.info("Game {} - {}{} while {} remains tapped", gameData.id,
                    target.getCard().getName(), describe(boost), entry.getCard().getName());
        }
    }

    /**
     * Renders the effect the way the card reads: a {@code +P/+T} boost, a keyword grant, or both.
     * A 0/0 boost with keywords (Hisoka's Guard) must not log a meaningless "+0/+0".
     */
    private String describe(BoostTargetCreatureWhileSourceTappedEffect boost) {
        StringBuilder text = new StringBuilder();
        if (boost.power() != 0 || boost.toughness() != 0 || boost.keywords().isEmpty()) {
            text.append(String.format(" gets %+d/%+d", boost.power(), boost.toughness()));
        }
        if (!boost.keywords().isEmpty()) {
            text.append(text.isEmpty() ? " has " : " and has ");
            text.append(boost.keywords().stream()
                    .map(keyword -> keyword.name().toLowerCase(Locale.ROOT).replace('_', ' '))
                    .collect(Collectors.joining(", ")));
        }
        return text.toString();
    }
}
