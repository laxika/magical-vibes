package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSameNameAsDyingCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import org.springframework.stereotype.Component;

/**
 * Adapts Remembrance's trigger-bound creature name to the shared restricted library-search
 * implementation.
 */
@Component
public class SearchLibraryForSameNameAsDyingCreatureToHandEffectHandler implements NormalEffectHandlerBean {

    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    public SearchLibraryForSameNameAsDyingCreatureToHandEffectHandler(
            SearchLibraryEffectHandler searchLibraryEffectHandler) {
        this.searchLibraryEffectHandler = searchLibraryEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForSameNameAsDyingCreatureToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForSameNameAsDyingCreatureToHandEffect sameNameEffect =
                (SearchLibraryForSameNameAsDyingCreatureToHandEffect) effect;
        if (sameNameEffect.dyingCreatureName() == null) {
            return;
        }

        searchLibraryEffectHandler.resolve(gameData, entry,
                new SearchLibraryEffect(
                        new CardNamedPredicate(sameNameEffect.dyingCreatureName()),
                        LibrarySearchDestination.HAND));
    }
}
