package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class BuffTargetCreatureIndefinitelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BuffTargetCreatureIndefinitelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var buff = (BuffTargetCreatureIndefinitelyEffect) effect;

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue; // Partially resolves — skip removed targets
            }

            // CR 611.2b: no stated duration means the continuous effect lasts indefinitely. Record
            // it as a PERMANENT floating continuous effect on the target — the layered pass reads
            // the +P/+Y (sublayer 7c) and the granted keywords (layer 6) off it for as long as the
            // permanent exists, so a second copy stacks additively rather than being replaced.
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(), buff,
                    target.getId(), null, null, EffectDuration.PERMANENT, 0));

            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .card(target.getCard())
                    .text(String.format(" gets %+d/%+d%s indefinitely.",
                            buff.powerBoost(), buff.toughnessBoost(),
                            buff.keywords().isEmpty() ? "" : " and gains " + formatKeywords(buff.keywords())))
                    .build());
            log.info("Game {} - {} gets {}/{} and {} indefinitely", gameData.id,
                    target.getCard().getName(), buff.powerBoost(), buff.toughnessBoost(), buff.keywords());
        }
    }

    private String formatKeywords(java.util.Set<Keyword> keywords) {
        return keywords.stream()
                .map(k -> k.name().charAt(0) + k.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
