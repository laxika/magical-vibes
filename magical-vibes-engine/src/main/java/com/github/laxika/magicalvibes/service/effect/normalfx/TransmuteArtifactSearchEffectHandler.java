package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransmuteArtifactSearchEffect;
import com.github.laxika.magicalvibes.model.effect.TransmuteArtifactSelectedCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransmuteArtifactSearchEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransmuteArtifactSearchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Permanent sacrificed = entry.getSacrificedPermanentSnapshot();
        if (controllerId == null || sacrificed == null) {
            return;
        }
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId, true)) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        CardTypePredicate artifactCard = new CardTypePredicate(CardType.ARTIFACT);
        List<Card> matchingCards = deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, artifactCard, null, gameData, controllerId))
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY))
                .toList();
        if (matchingCards.isEmpty()) {
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " searches their library but finds no artifact cards. Library is shuffled."));
            return;
        }

        int sacrificedManaValue = sacrificed.getCard().getManaValue();
        String prompt = "Search your library for an artifact card.";
        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(matchingCards))
                .reveals(true)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.HAND)
                .filterPredicate(artifactCard)
                .battlefieldIfManaValueAtMost(sacrificedManaValue)
                .shuffleAfterSelection(true)
                .followUp(LibrarySearchFollowUp.forSelectedCard(
                        new CardTruePredicate(), new TransmuteArtifactSelectedCardEffect()))
                .build();
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, params, prompt, true);
    }
}
