package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForNonlandCardAndRevealEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Grim Reminder's reveal-only nonland library search. */
@Component
@RequiredArgsConstructor
public class SearchLibraryForNonlandCardAndRevealEffectHandler implements NormalEffectHandlerBean {

    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForNonlandCardAndRevealEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForNonlandCardAndRevealEffect search =
                (SearchLibraryForNonlandCardAndRevealEffect) effect;
        librarySearchSupport.performLibrarySearch(
                gameData,
                entry.getControllerId(),
                card -> !card.hasType(CardType.LAND),
                "a nonland card",
                "Search your library for a nonland card to reveal.",
                true,
                true,
                LibrarySearchDestination.REVEAL_ONLY,
                LibrarySearchFollowUp.grimReminderSearch(search.lifeLoss()));
    }
}
