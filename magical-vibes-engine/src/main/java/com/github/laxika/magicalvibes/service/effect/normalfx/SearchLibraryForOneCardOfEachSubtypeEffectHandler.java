package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachSubtypeEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryForOneCardOfEachSubtypeEffect}. Either way the whole effect is one
 * search, recorded once even when the library is empty. A to-hand search (Gem of Becoming) is driven
 * through the shared {@link LibrarySearchSupport#startSingleSearchToHandPicks} descriptor queue so that
 * each subtype is a separate revealed pick and the library is shuffled once after the last subtype.
 * A battlefield search (Gaea's Balance) is one search through
 * {@link LibrarySearchSupport#startSubtypeToBattlefieldPicks}: the chosen cards enter the battlefield
 * together after the last pick.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForOneCardOfEachSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForOneCardOfEachSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForOneCardOfEachSubtypeEffect searchEffect =
                (SearchLibraryForOneCardOfEachSubtypeEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        if (searchEffect.destination() != LibrarySearchDestination.HAND) {
            librarySearchSupport.startSubtypeToBattlefieldPicks(gameData, controllerId,
                    searchEffect.subtypes(), searchEffect.requiredType(), searchEffect.destination());
            return;
        }

        librarySearchSupport.startSingleSearchToHandPicks(gameData, controllerId,
                LibrarySearchFollowUp.subtypeToHandPicks(searchEffect.subtypes(), searchEffect.basicOnly()));
    }
}
