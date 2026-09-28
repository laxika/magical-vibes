package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.NoLongerGoadedEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a timestamped snapshot that removes goad from the controller's creatures. */
@Component
@RequiredArgsConstructor
public class NoLongerGoadedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NoLongerGoadedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        NoLongerGoadedEffect noLongerGoaded = (NoLongerGoadedEffect) effect;
        for (Permanent permanent : gameData.playerBattlefields.get(entry.getControllerId())) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), noLongerGoaded,
                    permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        }
    }
}
