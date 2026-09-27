package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves the digital Seek keyword action without revealing or shuffling the library. */
@Component
@RequiredArgsConstructor
public class SeekLibraryToHandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekLibraryToHandEffect seek = (SeekLibraryToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            logNoMatch(gameData, entry);
            return;
        }

        List<Card> matchingCards = library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            logNoMatch(gameData, entry);
            return;
        }

        Card chosen = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.remove(chosen);
        gameData.playerHands.get(controllerId).add(chosen);
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks and puts " + chosen.getName() + " into hand.").build());
    }

    private void logNoMatch(GameData gameData, StackEntry entry) {
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks but finds no matching card.").build());
    }
}
