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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        List<Card> matchingCards = new ArrayList<>(result.topCards().stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.predicate(), sourceCardId, gameData, controllerId))
                .toList());

        Collections.shuffle(matchingCards);
        List<Card> chosenCards = matchingCards.subList(
                0, Math.min(Math.max(0, seek.maxMatches()), matchingCards.size()));
        List<Card> cardsToReturn = new ArrayList<>(result.topCards());
        for (Card chosenCard : chosenCards) {
            cardsToReturn.remove(chosenCard);
            gameData.playerHands.get(controllerId).add(chosenCard);
        }

        gameData.playerDecks.get(controllerId).addAll(cardsToReturn);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }
}
