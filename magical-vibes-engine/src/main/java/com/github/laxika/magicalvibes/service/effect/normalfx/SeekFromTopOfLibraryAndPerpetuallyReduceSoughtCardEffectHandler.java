package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random top-library card seek with a perpetual generic cost reduction. */
@Component
@RequiredArgsConstructor
public class SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffectHandler
        implements NormalEffectHandlerBean {

    private final LibraryRevealSupport libraryRevealSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var seek = (SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect) effect;
        UUID controllerId = entry.getControllerId();
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, seek.count());
        if (result == null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> matchingCards = result.topCards().stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), sourceCardId, gameData, controllerId))
                .toList();
        Card chosenCard = matchingCards.isEmpty()
                ? null
                : matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));

        List<Card> cardsToReturn = new ArrayList<>(result.topCards());
        if (chosenCard != null) {
            cardsToReturn.remove(chosenCard);
            gameData.playerHands.get(controllerId).add(chosenCard);
            gameData.perpetualCardCastCostReductions.merge(
                    chosenCard.getId(), seek.costReduction(), Integer::sum);
        }

        gameData.playerDecks.get(controllerId).addAll(cardsToReturn);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }
}
