package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekHighestManaValueCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a choice-free Seek for the highest-mana-value eligible card. */
@Component
@RequiredArgsConstructor
public class SeekHighestManaValueCardEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekHighestManaValueCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        SeekHighestManaValueCardEffect seek = (SeekHighestManaValueCardEffect) effect;
        int maximumManaValue = Math.max(0, amountEvaluationService.evaluate(
                gameData, seek.maximumManaValue(), AmountContext.forStackEntry(entry, null)));
        List<Card> matchingCards = library.stream()
                .filter(card -> !card.isToken() && card.getManaValue() <= maximumManaValue)
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        int highestManaValue = matchingCards.stream()
                .max(Comparator.comparingInt(Card::getManaValue))
                .orElseThrow()
                .getManaValue();
        List<Card> highestManaValueCards = matchingCards.stream()
                .filter(card -> card.getManaValue() == highestManaValue)
                .toList();
        Card selected = highestManaValueCards.get(ThreadLocalRandom.current().nextInt(highestManaValueCards.size()));
        library.remove(selected);
        gameData.addCardToHand(controllerId, selected);
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, List.of(selected));
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " seeks ", selected, " into their hand."));
    }
}
