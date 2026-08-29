package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SetSelfBasePowerToughnessIndefinitelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfBasePowerToughnessIndefinitelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetSelfBasePowerToughnessIndefinitelyEffect change =
                (SetSelfBasePowerToughnessIndefinitelyEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        long timestamp = gameData.nextTimestamp();
        source.setBasePowerOverriddenPermanently(true);
        source.setPermanentBasePowerOverride(change.power());
        source.setPermanentBasePowerOverrideTimestamp(timestamp);
        source.setBaseToughnessOverriddenPermanently(true);
        source.setPermanentBaseToughnessOverride(change.toughness());
        source.setPermanentBaseToughnessOverrideTimestamp(timestamp);

        SetBasePowerToughnessEffect setter = new SetBasePowerToughnessEffect(
                change.power(), change.toughness(), GrantScope.SELF);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), source.getId(), entry.getControllerId(), setter,
                source.getId(), null, null, EffectDuration.PERMANENT, 0));

        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" has base power and toughness " + change.power() + "/" + change.toughness() + ".")
                .build());
    }
}
