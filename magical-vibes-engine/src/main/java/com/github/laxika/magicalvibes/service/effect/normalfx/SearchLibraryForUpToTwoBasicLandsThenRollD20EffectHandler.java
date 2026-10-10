package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForUpToTwoBasicLandsThenRollD20Effect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchLibraryForUpToTwoBasicLandsThenRollD20EffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForUpToTwoBasicLandsThenRollD20Effect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryForUpToTwoBasicLandsThenRollD20Effect searchEffect =
                (SearchLibraryForUpToTwoBasicLandsThenRollD20Effect) effect;
        UUID controllerId = entry.getControllerId();
        LibrarySearchFollowUp followUp = LibrarySearchFollowUp.forD20BasicLandSearch(
                searchEffect.oneToNine(), searchEffect.tenToNineteen(), searchEffect.twenty());

        if (librarySearchSupport.isSearchPrevented(gameData, controllerId, false)) {
            insertRoll(gameData, entry, followUp, List.of());
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> matchingCards = deck == null ? List.of() : deck.stream()
                .filter(card -> card.hasType(CardType.LAND))
                .filter(card -> gameQueryService.cardHasSupertype(
                        card, com.github.laxika.magicalvibes.model.CardSupertype.BASIC,
                        gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " searches their library but finds no basic land cards. Library is shuffled."));
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, controllerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            insertRoll(gameData, entry, followUp, List.of());
            return;
        }

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(2)
                        .reveals(true)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.GIFTS_UNGIVEN_POOL)
                        .filterPredicate(CardPredicateUtils.basicLand())
                        .followUp(followUp)
                        .build(),
                "Search your library for up to two basic land cards to reveal.", true);
    }

    private void insertRoll(GameData gameData, StackEntry entry, LibrarySearchFollowUp followUp,
                            List<Card> cards) {
        RollD20Effect roll = followUp.selectedCardFollowUp().d20BasicLandSearch().rollEffect(cards);
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(roll));
    }
}
