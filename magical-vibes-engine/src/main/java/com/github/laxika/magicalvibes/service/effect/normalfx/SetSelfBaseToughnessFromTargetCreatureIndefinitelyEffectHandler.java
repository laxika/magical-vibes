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
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.Duration;

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
        gameData.addEffectOnPermanent(entry, new SetBasePowerToughnessEffect(
                null, toughness, GrantScope.TARGET, Duration.PERMANENT), source.getId(), Duration.PERMANENT);

        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" has base toughness " + toughness + " indefinitely.")
                .build());
    }
}
