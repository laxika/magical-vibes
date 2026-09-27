package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TurnSourceFaceUpEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ObjectProvider<GameService> gameServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnSourceFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.isFaceDown()) {
            return;
        }
        gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, source);
    }
}
