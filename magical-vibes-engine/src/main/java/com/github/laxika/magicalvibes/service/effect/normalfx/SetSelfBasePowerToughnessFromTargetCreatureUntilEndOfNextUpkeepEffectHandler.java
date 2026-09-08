package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessFromTargetCreatureUntilEndOfNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves Halfdane's upkeep ability by locking the target's current effective power and
 * toughness into a temporary layer-7b setter on Halfdane.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetSelfBasePowerToughnessFromTargetCreatureUntilEndOfNextUpkeepEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfBasePowerToughnessFromTargetCreatureUntilEndOfNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        int power = gameQueryService.getEffectivePower(gameData, target);
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);
        EffectDuration duration = EffectDuration.UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP;
        var setter = new SetBasePowerToughnessEffect(power, toughness, GrantScope.SELF, duration);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), setter, source.getId(), null, null, duration, 0));

        String description = " has base power and toughness " + power + "/" + toughness
                + " until the end of its controller's next upkeep.";
        gameLogService.append(gameData, GameLog.builder().card(source.getCard()).text(description).build());
        log.info("Game {} - {}{}", gameData.id, source.getCard().getName(), description);
    }
}
