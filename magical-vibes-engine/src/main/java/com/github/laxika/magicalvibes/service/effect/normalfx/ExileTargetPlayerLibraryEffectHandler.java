package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerLibraryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetPlayerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final LibraryExileSupport libraryExileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() != null) {
            libraryExileSupport.exileEntireLibrary(gameData, entry.getTargetId());
        }
    }
}
