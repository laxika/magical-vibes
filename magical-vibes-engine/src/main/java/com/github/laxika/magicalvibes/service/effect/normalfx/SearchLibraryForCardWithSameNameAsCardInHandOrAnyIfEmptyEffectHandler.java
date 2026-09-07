package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardWithSameNameAsCardInHandOrAnyIfEmptyEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Infernal Tutor's hand-dependent library search. */
@Component
@RequiredArgsConstructor
public class SearchLibraryForCardWithSameNameAsCardInHandOrAnyIfEmptyEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardWithSameNameAsCardInHandOrAnyIfEmptyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        if (hand.isEmpty()) {
            searchLibraryEffectHandler.resolve(gameData, entry, new SearchLibraryEffect());
            return;
        }

        playerInputService.beginInfernalTutorCardChoice(gameData, entry.getControllerId());
    }
}
