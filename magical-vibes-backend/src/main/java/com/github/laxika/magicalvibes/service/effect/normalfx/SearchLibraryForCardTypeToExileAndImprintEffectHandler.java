package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypeToExileAndImprintEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCardTypeToExileAndImprintEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardTypeToExileAndImprintEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForCardTypeToExileAndImprintEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                          SearchLibraryForCardTypeToExileAndImprintEffect effect) {
        gameData.imprintSourcePermanentId = entry.getSourcePermanentId();

        String typeText = librarySearchSupport.formatCardTypeSetForPrompt(effect.cardTypes());
        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> librarySearchSupport.matchesCardTypes(card, effect.cardTypes()),
                typeText + " cards",
                "Search your library for a " + typeText + " card to exile (imprint).",
                false,
                true,
                LibrarySearchDestination.EXILE_IMPRINT
        );
    }
}
