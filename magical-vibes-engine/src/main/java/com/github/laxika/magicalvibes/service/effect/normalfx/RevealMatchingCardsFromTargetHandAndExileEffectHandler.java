package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealMatchingCardsFromTargetHandAndExileEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Break Expectations-style filtered public hand reveals and controller choices. */
@Component
@RequiredArgsConstructor
public class RevealMatchingCardsFromTargetHandAndExileEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealMatchingCardsFromTargetHandAndExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealMatchingCardsFromTargetHandAndExileEffect revealEffect =
                (RevealMatchingCardsFromTargetHandAndExileEffect) effect;
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getTargetId(), List.of());
        List<Card> matchingCards = hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, revealEffect.filter(), entry.getCard().getId(), gameData, entry.getTargetId()))
                .toList();

        String targetName = gameData.playerIdToName.get(entry.getTargetId());
        GameLog.Builder log = GameLog.builder().text(targetName
                + " reveals cards with mana value 2 or greater from their hand");
        if (matchingCards.isEmpty()) {
            log.text(". No cards qualify.");
        } else {
            log.text(": ");
            appendCards(log, matchingCards).text(".");
        }
        gameLogService.append(gameData, log.build());
        cardRevealService.revealToAllPlayers(gameData, entry.getTargetId(),
                com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND, matchingCards);

        if (matchingCards.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedMatchingHandCardChoice(
                entry.getControllerId(), entry.getTargetId(), matchingCards, revealEffect.thenEffect(),
                "Choose a card to exile from " + targetName + "'s hand."));
    }

    private static GameLog.Builder appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        return builder;
    }
}
