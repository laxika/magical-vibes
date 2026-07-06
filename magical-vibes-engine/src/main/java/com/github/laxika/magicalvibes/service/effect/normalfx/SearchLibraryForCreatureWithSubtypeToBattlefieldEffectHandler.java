package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithSubtypeToBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCreatureWithSubtypeToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCreatureWithSubtypeToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCreatureWithSubtypeToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                                  SearchLibraryForCreatureWithSubtypeToBattlefieldEffect effect) {
        CardSubtype requiredSubtype = effect.requiredSubtype();
        String subtypeName = requiredSubtype.name().substring(0, 1) + requiredSubtype.name().substring(1).toLowerCase();

        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> {
                    boolean isCreatureCard = card.hasType(CardType.CREATURE);
                    return isCreatureCard && gameQueryService.cardHasSubtype(card, requiredSubtype, gameData, entry.getControllerId());
                },
                subtypeName + " creature card",
                "Search your library for a " + subtypeName + " creature card and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }
}
