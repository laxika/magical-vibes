package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryAndConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted library search whose next effect depends on the selected card. */
@Component
@RequiredArgsConstructor
public class SearchTargetLibraryAndConditionalEffectHandler implements NormalEffectHandlerBean {

    private final SearchTargetLibraryEffectHandler searchTargetLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchTargetLibraryAndConditionalEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchTargetLibraryAndConditionalEffect conditional =
                (SearchTargetLibraryAndConditionalEffect) effect;
        searchTargetLibraryEffectHandler.resolveForTargetPlayer(
                gameData,
                entry,
                new SearchTargetLibraryEffect(1, conditional.searchFilter(), conditional.destination(), false),
                entry.getTargetId(),
                LibrarySearchFollowUp.forSelectedCardWithManaValue(
                        conditional.selectedCardFilter(), conditional.conditionalEffect()));
    }
}
