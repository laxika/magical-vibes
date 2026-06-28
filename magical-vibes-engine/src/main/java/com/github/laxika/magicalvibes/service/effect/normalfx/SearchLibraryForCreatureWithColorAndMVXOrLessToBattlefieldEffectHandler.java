package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                                            SearchLibraryForCreatureWithColorAndMVXOrLessToBattlefieldEffect effect) {
        int maxMV = entry.getXValue();
        String colorName = effect.requiredColor().name().toLowerCase();

        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> card.hasType(CardType.CREATURE)
                        && card.getColors().contains(effect.requiredColor())
                        && card.getManaValue() <= maxMV,
                colorName + " creature card with mana value " + maxMV + " or less",
                "Search your library for a " + colorName + " creature card with mana value " + maxMV + " or less and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }
}
