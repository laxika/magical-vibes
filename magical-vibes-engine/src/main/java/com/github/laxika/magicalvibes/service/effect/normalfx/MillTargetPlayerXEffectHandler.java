package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerXEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MillTargetPlayerXEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillTargetPlayerXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillTargetPlayerXEffect) effect;
        int multiplier = entry.isCastWithFlashback() ? e.castWithFlashbackMultiplier() : 1;
        graveyardService.resolveMillPlayer(gameData, entry.getTargetId(), entry.getXValue() * multiplier);
    }
}
