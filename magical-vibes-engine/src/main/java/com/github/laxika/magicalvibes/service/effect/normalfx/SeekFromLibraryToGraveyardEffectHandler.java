package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryToGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random full-library seek that puts the found card into the graveyard. */
@Component
@RequiredArgsConstructor
public class SeekFromLibraryToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekFromLibraryToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null) {
            return;
        }

        SeekFromLibraryToGraveyardEffect seek = (SeekFromLibraryToGraveyardEffect) effect;
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> matchingCards = library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.predicate(), sourceCardId, gameData, controllerId))
                .toList();
        if (!matchingCards.isEmpty()) {
            Card chosenCard = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
            library.remove(chosenCard);
            library.add(0, chosenCard);
            if (!graveyardService.resolvePutTopCardsIntoGraveyard(gameData, controllerId, 1).isEmpty()) {
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(controllerId) + " puts ", chosenCard,
                        " into their graveyard."));
            }
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }
}
