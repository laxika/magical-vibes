package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryWithGreaterManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random full-library seek constrained by mana value. */
@Component
@RequiredArgsConstructor
public class SeekFromLibraryWithGreaterManaValueEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekFromLibraryWithGreaterManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        SeekFromLibraryWithGreaterManaValueEffect seek =
                (SeekFromLibraryWithGreaterManaValueEffect) effect;
        int referenceManaValue = amountEvaluationService.evaluate(
                gameData, seek.referenceManaValue(), AmountContext.forStackEntry(entry, null));
        List<Card> matchingCards = library.stream()
                .filter(card -> card.getManaValue() > referenceManaValue)
                .toList();
        Card chosenCard = matchingCards.isEmpty()
                ? null
                : matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));

        if (chosenCard != null) {
            library.remove(chosenCard);
            exileService.exileCard(gameData, controllerId, chosenCard);
            exileSupport.grantPlayUntilOwnersNextTurn(gameData, chosenCard.getId(), controllerId);
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " seeks and exiles ", chosenCard,
                    "; it may be played until the end of the next turn."));
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }
}
