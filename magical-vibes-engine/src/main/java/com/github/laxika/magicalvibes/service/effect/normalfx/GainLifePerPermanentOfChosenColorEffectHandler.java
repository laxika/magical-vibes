package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerPermanentOfChosenColorEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainLifePerPermanentOfChosenColorEffect} by pausing for a color choice; the
 * choice handler performs the battlefield count and life gain.
 */
@Component
@RequiredArgsConstructor
public class GainLifePerPermanentOfChosenColorEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifePerPermanentOfChosenColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInputService.beginGainLifePerPermanentOfChosenColorChoice(gameData, entry.getControllerId(),
                entry.getCard(), entry.getEntryType());
    }
}
