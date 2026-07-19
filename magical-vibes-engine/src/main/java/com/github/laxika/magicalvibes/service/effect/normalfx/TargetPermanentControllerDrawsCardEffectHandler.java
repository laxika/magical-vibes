package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPermanentControllerDrawsCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPermanentControllerDrawsCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, entry.getTargetId());
        if (controllerId != null) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
    }
}
