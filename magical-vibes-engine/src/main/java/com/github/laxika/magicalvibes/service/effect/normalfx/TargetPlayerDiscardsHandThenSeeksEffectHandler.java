package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenSeeksEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a target player's hand discard followed by one Seek per discarded card. */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsHandThenSeeksEffectHandler implements NormalEffectHandlerBean {

    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsHandThenSeeksEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayerDiscardsHandThenSeeksEffect seekEffect =
                (TargetPlayerDiscardsHandThenSeeksEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        int discarded = discardHandEffectHandler.discardHand(
                gameData, targetPlayerId, entry.getControllerId(), entry.getCard().getName());
        if (discarded == 0) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        for (int i = 0; i < discarded; i++) {
            List<Card> matchingCards = new ArrayList<>(library.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, seekEffect.seekFilter(), null, gameData, targetPlayerId))
                    .toList());
            if (matchingCards.isEmpty()) {
                gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                        .text("seeks but finds no matching card.").build());
                break;
            }

            Card chosen = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
            library.remove(chosen);
            gameData.addCardToHand(targetPlayerId, chosen);
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text("seeks and puts " + chosen.getName() + " into hand.").build());
        }
    }
}
