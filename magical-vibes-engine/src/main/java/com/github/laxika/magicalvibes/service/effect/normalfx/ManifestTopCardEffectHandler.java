package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves manifesting the top card of a library. */
@Component
@RequiredArgsConstructor
public class ManifestTopCardEffectHandler implements NormalEffectHandlerBean {

    private final ManifestService manifestService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        manifestService.manifestTopCard(gameData, entry.getControllerId(), entry.getCard());
    }
}
