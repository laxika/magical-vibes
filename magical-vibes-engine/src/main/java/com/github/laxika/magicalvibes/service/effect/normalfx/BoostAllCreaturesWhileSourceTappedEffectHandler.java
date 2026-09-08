package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.BuffTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a global source-tapped creature boost by reusing the existing per-creature floating boost path. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BoostAllCreaturesWhileSourceTappedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostAllCreaturesWhileSourceTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostAllCreaturesWhileSourceTappedEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.isTapped()) {
            return;
        }

        List<Permanent> creatures = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        });

        for (Permanent creature : creatures) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), sourcePermanentId,
                    entry.getControllerId(),
                    new BuffTargetCreatureIndefinitelyEffect(boost.powerBoost(), boost.toughnessBoost()),
                    creature.getId(), null, null, EffectDuration.WHILE_SOURCE_TAPPED, 0));
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(String.format(" gives %+d/%+d to %d creature(s) for as long as it remains tapped.",
                        boost.powerBoost(), boost.toughnessBoost(), creatures.size()))
                .build());
        log.info("Game {} - {} gives {}/{} to {} creatures while tapped", gameData.id,
                entry.getCard().getName(), boost.powerBoost(), boost.toughnessBoost(), creatures.size());
    }
}
