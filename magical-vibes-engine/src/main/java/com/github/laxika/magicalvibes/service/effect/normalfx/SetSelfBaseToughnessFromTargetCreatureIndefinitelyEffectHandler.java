package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBaseToughnessFromTargetCreatureIndefinitelyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetSelfBaseToughnessFromTargetCreatureIndefinitelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfBaseToughnessFromTargetCreatureIndefinitelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        int toughness = gameQueryService.getEffectivePower(gameData, target) + 1;
        source.setBaseToughnessOverriddenPermanently(true);
        source.setPermanentBaseToughnessOverride(toughness);
        source.setPermanentBaseToughnessOverrideTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" has base toughness " + toughness + " indefinitely.")
                .build());
    }
}
