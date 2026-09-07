package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfControllerLifeTotalAtLeastNotedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the noted-life-total upkeep draw and updates the note afterward. */
@Component
@RequiredArgsConstructor
public class DrawCardIfControllerLifeTotalAtLeastNotedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardIfControllerLifeTotalAtLeastNotedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        int currentLife = gameData.getLife(entry.getControllerId());
        if (currentLife >= source.getChosenNumber()) {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
        }
        source.setChosenNumber(currentLife);
    }
}
