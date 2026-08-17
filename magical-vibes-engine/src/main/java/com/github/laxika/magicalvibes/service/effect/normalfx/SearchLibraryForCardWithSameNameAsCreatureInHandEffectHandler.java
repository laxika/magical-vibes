package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardWithSameNameAsCreatureInHandEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Assembly Hall's creature-card-in-hand choice. */
@Component
@RequiredArgsConstructor
public class SearchLibraryForCardWithSameNameAsCreatureInHandEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardWithSameNameAsCreatureInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        playerInputService.beginAssemblyHallCreatureCardChoice(gameData, entry.getControllerId());
    }
}
