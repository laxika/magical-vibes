package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToTopOfLibraryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCardToTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardToTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCardToTopOfLibraryEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry, SearchLibraryForCardToTopOfLibraryEffect effect) {
        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> true,
                "cards",
                "Search your library for a card, then shuffle and put that card on top.",
                false,
                false,
                LibrarySearchDestination.TOP_OF_LIBRARY
        );
    }
}
