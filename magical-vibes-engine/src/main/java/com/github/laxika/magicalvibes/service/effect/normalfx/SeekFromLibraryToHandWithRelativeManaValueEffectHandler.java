package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryToHandWithRelativeManaValueEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random full-library seek constrained by a relative mana value. */
@Component
@RequiredArgsConstructor
public class SeekFromLibraryToHandWithRelativeManaValueEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekFromLibraryToHandWithRelativeManaValueEffect.class;
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

        SeekFromLibraryToHandWithRelativeManaValueEffect seek =
                (SeekFromLibraryToHandWithRelativeManaValueEffect) effect;
        int referenceManaValue = amountEvaluationService.evaluate(
                gameData, seek.referenceManaValue(), AmountContext.forStackEntry(entry, null));
        List<Card> matchingCards = library.stream()
                .filter(card -> seek.greaterThan()
                        ? card.getManaValue() > referenceManaValue
                        : card.getManaValue() < referenceManaValue)
                .toList();
        if (!matchingCards.isEmpty()) {
            Card chosenCard = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
            library.remove(chosenCard);
            gameData.addCardToHand(controllerId, chosenCard);
        }

        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }
}
