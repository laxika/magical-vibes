package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileControllerLibraryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileControllerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final LibraryExileSupport libraryExileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileControllerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        libraryExileSupport.exileEntireLibrary(gameData, entry.getControllerId());
    }
}
