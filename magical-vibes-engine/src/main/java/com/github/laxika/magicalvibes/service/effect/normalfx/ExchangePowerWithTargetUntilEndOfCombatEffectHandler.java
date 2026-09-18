package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.ExchangePowerWithTargetUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the power exchange by installing two locked layer-7b power setters. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangePowerWithTargetUntilEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangePowerWithTargetUntilEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        int sourcePower = gameQueryService.getEffectivePower(gameData, source);
        int targetPower = gameQueryService.getEffectivePower(gameData, target);
        EffectDuration duration = EffectDuration.UNTIL_END_OF_COMBAT;

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(),
                new SetBasePowerToughnessEffect(targetPower, null, GrantScope.TARGET, duration),
                source.getId(), null, null, duration, 0));
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(),
                new SetBasePowerToughnessEffect(sourcePower, null, GrantScope.TARGET, duration),
                target.getId(), null, null, duration, 0));

        gameLogService.append(gameData, GameLog.cardTextCard(source.getCard(),
                " exchanges power with ", target.getCard(), " until end of combat."));
        log.info("Game {} - {} exchanges power with {} until end of combat", gameData.id,
                source.getCard().getName(), target.getCard().getName());
    }
}
