package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreaturesWithTotalManaValueAtMostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a cumulative-mana-value creature library search through the shared search flow. */
@Component
@RequiredArgsConstructor
public class SearchLibraryForCreaturesWithTotalManaValueAtMostEffectHandler
        implements NormalEffectHandlerBean {

    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreaturesWithTotalManaValueAtMostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForCreaturesWithTotalManaValueAtMostEffect totalManaValueEffect =
                (SearchLibraryForCreaturesWithTotalManaValueAtMostEffect) effect;
        searchLibraryEffectHandler.resolveWithTotalManaValueCap(
                gameData,
                entry,
                new SearchLibraryEffect(
                        new CardsInLibrary(CountScope.CONTROLLER),
                        new CardTypePredicate(CardType.CREATURE),
                        LibrarySearchDestination.BATTLEFIELD),
                totalManaValueEffect.maxTotalManaValue());
    }
}
