package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a draw for the controller of the permanent that caused the trigger. */
@Component
@RequiredArgsConstructor
public class DrawCardForTriggeringPermanentControllerEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardForTriggeringPermanentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getTriggeringPermanentControllerId();
        if (entry.getTriggeringPermanentId() != null) {
            UUID liveControllerId = gameQueryService.findPermanentController(gameData, entry.getTriggeringPermanentId());
            if (liveControllerId != null) {
                controllerId = liveControllerId;
            }
        }
        if (controllerId != null && gameData.playerIds.contains(controllerId)) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
    }
}
