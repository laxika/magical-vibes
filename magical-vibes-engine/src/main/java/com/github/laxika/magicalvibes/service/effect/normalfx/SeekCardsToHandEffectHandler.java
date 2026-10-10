package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves digital seek by randomly moving matching library cards to hand. */
@Component
@RequiredArgsConstructor
public class SeekCardsToHandEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekCardsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        seek(gameData, entry, (SeekCardsToHandEffect) effect);
    }

    /** Resolves a Seek and returns the exact cards moved into the controller's hand. */
    public List<Card> seek(GameData gameData, StackEntry entry, SeekCardsToHandEffect seek) {
        UUID controllerId = entry.getControllerId();
        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, seek.amount(), AmountContext.forStackEntry(entry, null)));
        if (count == 0) {
            return List.of();
        }

        List<Card> library = gameData.playerDecks.get(controllerId);
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (library == null || hand == null || library.isEmpty()) {
            return List.of();
        }

        ManaValueBound manaValueBound = seek.manaValueBound();
        Integer boundValue = manaValueBound == null ? null
                : amountEvaluationService.evaluate(gameData, manaValueBound.amount(),
                        AmountContext.forStackEntry(entry, null)) + manaValueBound.offset();

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> !card.isToken())
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId))
                .filter(card -> matchesManaValue(card, boundValue, manaValueBound))
                .toList());
        Collections.shuffle(matchingCards);

        int soughtCount = Math.min(count, matchingCards.size());
        List<Card> soughtCards = new ArrayList<>(matchingCards.subList(0, soughtCount));
        for (Card card : soughtCards) {
            library.remove(card);
            hand.add(card);
        }

        if (soughtCount > 0) {
            triggerCollectionService.checkSeekTriggers(gameData, controllerId, soughtCards);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " seeks " + soughtCount + " card"
                            + (soughtCount == 1 ? "" : "s") + "."));
        }
        return soughtCards;
    }

    private boolean matchesManaValue(Card card, Integer boundValue, ManaValueBound bound) {
        if (boundValue == null) {
            return true;
        }
        return bound.exact()
                ? card.getManaValue() == boundValue
                : card.getManaValue() <= boundValue;
    }
}
