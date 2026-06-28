package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithExactMVToBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCreatureWithExactMVToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreatureWithExactMVToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCreatureWithExactMVToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                                 SearchLibraryForCreatureWithExactMVToBattlefieldEffect effect) {
        int targetMV = entry.getXValue() + effect.mvOffset();

        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> (card.hasType(CardType.CREATURE))
                        && card.getManaValue() == targetMV,
                "creature card with mana value " + targetMV,
                "Search your library for a creature card with mana value " + targetMV + " and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }
}
