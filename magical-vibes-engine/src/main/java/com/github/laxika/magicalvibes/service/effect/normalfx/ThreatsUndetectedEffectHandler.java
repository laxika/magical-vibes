package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ThreatsUndetectedEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Threats Undetected's different-power creature search and opponent choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThreatsUndetectedEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ThreatsUndetectedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return;
        }

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            return;
        }

        CardTypePredicate creaturePredicate = new CardTypePredicate(CardType.CREATURE);
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> matchingCards = deck == null ? List.of() : deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, creaturePredicate, null, gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " searches their library but finds no "
                            + CardPredicateUtils.describeFilter(creaturePredicate) + ". Library is shuffled."));
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId, List.of(),
                List.of(), Map.of(), List.of(), List.of(),
                com.github.laxika.magicalvibes.model.CardPileDisposition.THREATS_UNDETECTED));

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                        .remainingCount(4)
                        .reveals(true)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.THREATS_UNDETECTED_POOL)
                        .filterPredicate(creaturePredicate)
                        .requireDifferentPowers(true)
                        .build(),
                "Search your library for a creature card with a different power to reveal (4 remaining).",
                true);

        log.info("Game {} - {} begins a Threats Undetected library search", gameData.id, playerName);
    }
}
