package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameLogService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
            SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return;
        }

        CardSubtype subtype = effect.subtype();
        List<Card> matchingCards = deck.stream()
                .filter(card -> !effect.basicOnly()
                        || (card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC)))
                .filter(card -> subtype == null || card.getSubtypes().contains(subtype))
                .toList();

        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            String logMsg = playerName + " searches their library but finds no "
                    + describeCardType(effect) + " cards. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return;
        }

        int toHandCount = 1 + (effect.extraCardCondition() != null && conditionEvaluationService.isMet(
                gameData, effect.extraCardCondition(), ConditionContext.forStackEntry(entry)) ? 1 : 0);
        String cardDescription = describeCardType(effect);

        // First pick: land to battlefield tapped (no shuffle yet); the follow-up
        // hand search rides the search interaction
        LibrarySearchFollowUp followUp = effect.basicOnly()
                ? LibrarySearchFollowUp.forBasicLandToHand(toHandCount, subtype)
                : LibrarySearchFollowUp.forLandSubtypeToHand(toHandCount, subtype);
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                .shuffleAfterSelection(false)
                .followUp(followUp)
                .build(), "Search your library for a " + cardDescription + " card to put onto the battlefield tapped.", true);

        log.info("Game {} - {} searches library for {} {} cards", gameData.id, playerName,
                matchingCards.size(), cardDescription);
    }

    private String describeCardType(SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect effect) {
        if (effect.subtype() == null) {
            return effect.basicOnly() ? "basic land" : "land";
        }
        return (effect.basicOnly() ? "basic " : "") + effect.subtype().getDisplayName();
    }
}
