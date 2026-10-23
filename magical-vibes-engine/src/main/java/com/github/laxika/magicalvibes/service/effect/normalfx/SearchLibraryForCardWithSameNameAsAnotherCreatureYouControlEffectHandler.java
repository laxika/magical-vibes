package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Pattern Matcher's library search for a card with the same name as any other creature the
 * resolving controller controls. No creature is chosen, so a "can't be chosen" creature's name still
 * counts; with no other creature the search finds nothing but still happens.
 */
@Component
@RequiredArgsConstructor
public class SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardWithSameNameAsAnotherCreatureYouControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<CardPredicate> sameName = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            if (!permanent.getId().equals(sourcePermanentId) && gameQueryService.isCreature(gameData, permanent)) {
                sameName.add(new CardNamedPredicate(permanent.getCard().getName()));
            }
        }

        searchLibraryEffectHandler.resolve(gameData, entry,
                new SearchLibraryEffect(new CardAnyOfPredicate(sameName), LibrarySearchDestination.HAND));
    }
}
