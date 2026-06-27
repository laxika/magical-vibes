package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureToTopOfLibraryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCreatureToTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreatureToTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCreatureToTopOfLibraryEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry, SearchLibraryForCreatureToTopOfLibraryEffect effect) {
        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> card.hasType(CardType.CREATURE),
                "creature cards",
                "Search your library for a creature card, reveal it, then shuffle and put that card on top.",
                true,
                true,
                LibrarySearchDestination.TOP_OF_LIBRARY
        );
    }
}
