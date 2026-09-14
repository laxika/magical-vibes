package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SourcePermanentControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves life loss by the source permanent's current, or last-known, controller. */
@Component
@RequiredArgsConstructor
public class SourcePermanentControllerLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourcePermanentControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SourcePermanentControllerLosesLifeEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID controllerId = source == null
                ? e.sourceControllerId()
                : gameQueryService.findPermanentController(gameData, source.getId());
        if (controllerId != null && gameData.playerIds.contains(controllerId) && e.amount() > 0) {
            lifeSupport.applyLifeLoss(gameData, controllerId, e.amount(), entry.getCard().getName());
        }
    }
}
