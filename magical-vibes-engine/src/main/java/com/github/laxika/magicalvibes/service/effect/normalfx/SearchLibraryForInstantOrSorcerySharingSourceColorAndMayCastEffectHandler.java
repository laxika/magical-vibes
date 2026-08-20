package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForInstantOrSorcerySharingSourceColorAndMayCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SearchLibraryForInstantOrSorcerySharingSourceColorAndMayCastEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForInstantOrSorcerySharingSourceColorAndMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent liveSource = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent source = liveSource != null ? liveSource : entry.getSourcePermanentSnapshot();
        if (source == null) {
            return;
        }

        Set<CardColor> colors = liveSource != null
                ? gameQueryService.getEffectiveColors(gameData, liveSource)
                : Set.copyOf(source.getEffectiveColors());
        CardPredicate filter = new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                new CardAnyOfPredicate(colors.stream().<CardPredicate>map(CardColorPredicate::new).toList())
        ));

        searchLibraryEffectHandler.resolveWithFollowUp(
                gameData,
                entry,
                new SearchLibraryEffect(filter, LibrarySearchDestination.EXILE_FOR_MAY_CAST),
                com.github.laxika.magicalvibes.model.LibrarySearchFollowUp.NONE);
    }
}
