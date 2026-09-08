package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandOrCreateTokenEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a Food-style search that creates a token when no card reaches the hand. */
@Component
@RequiredArgsConstructor
public class SearchLibraryForCardToHandOrCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardToHandOrCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForCardToHandOrCreateTokenEffect searchOrToken =
                (SearchLibraryForCardToHandOrCreateTokenEffect) effect;
        searchLibraryEffectHandler.resolveWithFollowUp(
                gameData,
                entry,
                new SearchLibraryEffect(searchOrToken.filter()),
                LibrarySearchFollowUp.forNoCard(searchOrToken.tokenTemplate()));
    }
}
