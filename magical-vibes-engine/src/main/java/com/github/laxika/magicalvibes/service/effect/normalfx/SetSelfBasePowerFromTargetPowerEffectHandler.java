package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerFromTargetPowerEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SetSelfBasePowerFromTargetPowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfBasePowerFromTargetPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        int power = gameQueryService.getEffectivePower(gameData, target);
        var setter = new SetBasePowerToughnessEffect(power, null, GrantScope.SELF);
        FloatingContinuousEffect floating = gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), source.getId(), entry.getControllerId(),
                setter, source.getId(), null, null, EffectDuration.PERMANENT, 0));
        source.setBasePowerOverriddenPermanently(true);
        source.setPermanentBasePowerOverride(power);
        source.setPermanentBasePowerOverrideTimestamp(floating.timestamp());

        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" has base power " + power + " indefinitely.")
                .build());
    }
}
