package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves life loss by the controller of the damage source captured by a trigger. */
@Component
@RequiredArgsConstructor
public class DamageSourceControllerLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageSourceControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageSourceControllerLosesLifeEffect) effect;
        UUID playerId = e.sourceControllerId();
        if (e.currentControllerAtResolution() && entry.getTriggeringPermanentId() != null) {
            UUID currentController = gameQueryService.findPermanentController(gameData, entry.getTriggeringPermanentId());
            if (currentController != null) playerId = currentController;
        }
        if (playerId == null || e.amount() <= 0 || !gameData.playerIds.contains(playerId)) return;

        lifeSupport.applyLifeLoss(gameData, playerId, e.amount(), entry.getCard().getName());
    }
}
