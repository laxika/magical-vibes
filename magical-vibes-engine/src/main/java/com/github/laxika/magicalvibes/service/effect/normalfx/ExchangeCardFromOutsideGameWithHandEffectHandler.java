package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeCardFromOutsideGameWithHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the first choice of a card exchange between outside the game and the hand. */
@Component
@RequiredArgsConstructor
public class ExchangeCardFromOutsideGameWithHandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeCardFromOutsideGameWithHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExchangeCardFromOutsideGameWithHandEffect exchange =
                (ExchangeCardFromOutsideGameWithHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        if (hand.isEmpty()) {
            return;
        }

        List<Card> matchingCards = com.github.laxika.magicalvibes.service.OutsideGameCards.view(gameData, controllerId).stream()
                .filter(card -> exchange.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        card, exchange.filter(), null, gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " finds no "
                            + CardPredicateUtils.describeFilter(exchange.filter())
                            + " card outside the game."));
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExchangeOutsideGameCardChoice(
                controllerId, new ArrayList<>(matchingCards), exchange.filter()));
    }
}
