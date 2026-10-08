package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Lets the controller choose a named card from either permitted search zone. */
@Component
@RequiredArgsConstructor
public class SearchLibraryAndOrGraveyardForNamedCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final SearchLibraryAndOrGraveyardForCardToHandEffectHandler searchHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndOrGraveyardForNamedCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var search = (SearchLibraryAndOrGraveyardForNamedCardToHandEffect) effect;
        searchHandler.resolve(gameData, entry, new SearchLibraryAndOrGraveyardForCardToHandEffect(
                new CardNamedPredicate(search.cardName())));
    }
}
