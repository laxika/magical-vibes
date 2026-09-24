package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a hidden random selection from the top of a library. */
@Component
@RequiredArgsConstructor
public class SeekFromTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekFromTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var seek = (SeekFromTopOfLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, Math.max(0, seek.count()));
        if (result == null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> matchingCards = result.topCards().stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.predicate(), sourceCardId, gameData, controllerId))
                .toList();

        Card chosenCard = matchingCards.isEmpty()
                ? null
                : matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        List<Card> cardsToReturn = new ArrayList<>(result.topCards());
        if (chosenCard != null) {
            cardsToReturn.remove(chosenCard);
            gameData.playerHands.get(controllerId).add(chosenCard);
        }

        gameData.playerDecks.get(controllerId).addAll(cardsToReturn);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }
}
