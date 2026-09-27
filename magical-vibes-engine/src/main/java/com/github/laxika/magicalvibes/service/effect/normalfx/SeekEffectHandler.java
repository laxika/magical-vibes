package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class SeekEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekEffect seekEffect = (SeekEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> matchingCards = library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seekEffect.predicate(), sourceCardId, gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        Card selected = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.remove(selected);
        gameData.addCardToHand(controllerId, selected);
        if (seekEffect.storeSelectedCard()) {
            entry.setChosenObjectCard(selected);
        }
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, List.of(selected));
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " seeks ", selected, " into their hand."));
    }
}
