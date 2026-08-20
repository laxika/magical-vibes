package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
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
        searchLibraryEffectHandler.resolveWithFollowUp(
                gameData,
                entry,
                new SearchLibraryEffect(conditional.searchFilter(), conditional.destination()),
                LibrarySearchFollowUp.forSelectedCard(
                        conditional.selectedCardFilter(), conditional.conditionalEffect()));
    }
}
