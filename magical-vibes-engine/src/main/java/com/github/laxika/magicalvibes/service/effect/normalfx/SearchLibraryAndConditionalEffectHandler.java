package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a library search whose next effect depends on the selected card. */
@Component
@RequiredArgsConstructor
public class SearchLibraryAndConditionalEffectHandler implements NormalEffectHandlerBean {

    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndConditionalEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryAndConditionalEffect conditional = (SearchLibraryAndConditionalEffect) effect;
        SearchLibraryEffect search = new SearchLibraryEffect(
                new Fixed(1), conditional.searchFilter(), conditional.destination(), null, 1, false,
                false, false, false, null, LibrarySearchPlayer.CONTROLLER, false, false,
                conditional.shuffleAfterSelection());
        searchLibraryEffectHandler.resolveWithFollowUp(
                gameData,
                entry,
                search,
                LibrarySearchFollowUp.forSelectedCard(
                        conditional.selectedCardFilter(), conditional.conditionalEffect()));
    }
}
