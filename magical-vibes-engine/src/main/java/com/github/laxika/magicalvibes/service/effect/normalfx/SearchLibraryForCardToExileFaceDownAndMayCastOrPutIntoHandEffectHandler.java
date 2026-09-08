package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToExileFaceDownAndMayCastOrPutIntoHandEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLibraryForCardToExileFaceDownAndMayCastOrPutIntoHandEffectHandler
        implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardToExileFaceDownAndMayCastOrPutIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForCardToExileFaceDownAndMayCastOrPutIntoHandEffect searchEffect =
                (SearchLibraryForCardToExileFaceDownAndMayCastOrPutIntoHandEffect) effect;
        Integer maxManaValue = entry.wasKicked() ? searchEffect.maxManaValue() : null;
        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> true,
                "cards",
                "Search your library for a card to exile face down, then shuffle.",
                false,
                false,
                LibrarySearchDestination.EXILE_FACE_DOWN_AND_MAY_CAST_OR_PUT_INTO_HAND,
                LibrarySearchFollowUp.NONE,
                null,
                maxManaValue);
    }
}
