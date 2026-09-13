package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsSharingTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchLibraryForUpToTwoBasicLandsSharingTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForUpToTwoBasicLandsSharingTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForUpToTwoBasicLandsSharingTypeEffect searchEffect =
                (SearchLibraryForUpToTwoBasicLandsSharingTypeEffect) effect;
        if (!searchEffect.secondPick()) {
            searchLibraryEffectHandler.resolveWithFollowUp(
                    gameData,
                    entry,
                    new SearchLibraryEffect(new Fixed(1), CardPredicateUtils.basicLand(),
                            LibrarySearchDestination.BATTLEFIELD_TAPPED),
                    LibrarySearchFollowUp.forSelectedCard(
                            new CardTruePredicate(),
                            new SearchLibraryForUpToTwoBasicLandsSharingTypeEffect(true)));
            return;
        }

        UUID controllerId = entry.getControllerId();
        Card firstPick = entry.getChosenObjectCard();
        if (controllerId == null || firstPick == null) {
            return;
        }

        Set<CardSubtype> firstLandTypes = gameQueryService.landTypesOf(firstPick);
        librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> isBasicLand(gameData, card, controllerId)
                        && !gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.LIBRARY)
                        && gameQueryService.landTypesOf(card).stream().anyMatch(firstLandTypes::contains),
                "a basic land card that shares a land type with the first land",
                "Search your library for up to one basic land card that shares a land type with the first land, "
                        + "put it onto the battlefield tapped, then shuffle.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD_TAPPED
        );
    }

    private boolean isBasicLand(GameData gameData, Card card, UUID controllerId) {
        return predicateEvaluationService.matchesCardPredicate(
                card, CardPredicateUtils.basicLand(), null, gameData, controllerId);
    }
}
