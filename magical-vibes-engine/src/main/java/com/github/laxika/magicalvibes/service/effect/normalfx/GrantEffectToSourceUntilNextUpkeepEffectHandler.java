package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantEffectToSourceUntilNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectToSourceUntilNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantEffectToSourceUntilNextUpkeepEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        GrantTriggeredAbilityEffect continuousGrant = new GrantTriggeredAbilityEffect(
                grant.slot(), grant.grantedEffect(), GrantScope.SELF);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), sourceId, entry.getControllerId(),
                continuousGrant, sourceId, null, null,
                EffectDuration.UNTIL_CONTROLLERS_NEXT_UPKEEP, 0));

        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" gains a triggered ability until your next upkeep.")
                .build());
        log.info("Game {} - {} gains a {} ability until the controller's next upkeep",
                gameData.id, source.getCard().getName(), grant.slot());
    }
}
