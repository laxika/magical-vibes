package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DiscardSpecificCardAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Arius's seek and binds the selected card to its delayed discard trigger. */
@Component
@RequiredArgsConstructor
public class SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var seek = (SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect) effect;
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
        gameData.queueDelayedAction(new DiscardSpecificCardAtNextEndStep(
                controllerId, chosen.getId(), entry.getCard()));
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks and puts " + chosen.getName() + " into hand.").build());
    }

    private void logNoMatch(GameData gameData, StackEntry entry) {
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks but finds no matching card.").build());
    }
}
