package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachColorToGraveyardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryForOneCardOfEachColorToGraveyardEffect}: one search (recorded once, even
 * with an empty library) whose per-colour picks are driven through
 * {@link LibrarySearchSupport#startColorPicks}; the found cards enter the graveyard together after the
 * last pick and the library is shuffled once.
 */
@Component
@RequiredArgsConstructor
public class SearchLibraryForOneCardOfEachColorToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForOneCardOfEachColorToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForOneCardOfEachColorToGraveyardEffect searchEffect =
                (SearchLibraryForOneCardOfEachColorToGraveyardEffect) effect;
        librarySearchSupport.startColorPicks(gameData, entry.getControllerId(), searchEffect.colors(),
                LibrarySearchDestination.GRAVEYARD);
    }
}
