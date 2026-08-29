package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SetSelfColorIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SetSelfColorIndefinitelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfColorIndefinitelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetSelfColorIndefinitelyEffect change = (SetSelfColorIndefinitelyEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        SetTargetColorEffect setter = new SetTargetColorEffect(change.color());
        source.getTransientColors().clear();
        source.getTransientColors().add(change.color());
        source.setColorOverridden(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), source.getId(), entry.getControllerId(), setter,
                source.getId(), null, null, EffectDuration.PERMANENT, 0));

        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" becomes " + change.color().name().toLowerCase() + ".").build());
    }
}
