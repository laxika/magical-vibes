package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithMVXOrLessToHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCreatureWithMVXOrLessToHandEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreatureWithMVXOrLessToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCreatureWithMVXOrLessToHandEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry, SearchLibraryForCreatureWithMVXOrLessToHandEffect effect) {
        int maxMV = entry.getXValue();

        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> card.hasType(CardType.CREATURE) && card.getManaValue() <= maxMV,
                "creature card with mana value " + maxMV + " or less",
                "Search your library for a creature card with mana value " + maxMV + " or less to reveal and put into your hand.",
                true,
                true,
                LibrarySearchDestination.HAND
        );
    }
}
