package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndPerpetuallyReduceSoughtCardsEffect;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a multi-card Seek that perpetually reduces the exact cards it finds. */
@Component
public class SeekLibraryAndPerpetuallyReduceSoughtCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    public SeekLibraryAndPerpetuallyReduceSoughtCardsEffectHandler(
            PredicateEvaluationService predicateEvaluationService,
            AmountEvaluationService amountEvaluationService,
            TriggerCollectionService triggerCollectionService) {
        this.predicateEvaluationService = predicateEvaluationService;
        this.amountEvaluationService = amountEvaluationService;
        this.triggerCollectionService = triggerCollectionService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryAndPerpetuallyReduceSoughtCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekLibraryAndPerpetuallyReduceSoughtCardsEffect seek =
                (SeekLibraryAndPerpetuallyReduceSoughtCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        AmountContext context = AmountContext.forStackEntry(entry, null);
        int count = amountEvaluationService.evaluate(gameData, seek.count(), context);
        int reduction = amountEvaluationService.evaluate(
                gameData, seek.genericCastCostReduction(), context);
        if (library == null || library.isEmpty() || count <= 0) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId))
                .toList());
        if (matchingCards.isEmpty()) {
            return;
        }

        List<Card> soughtCards = new ArrayList<>();
        int cardsToSeek = Math.min(count, matchingCards.size());
        for (int i = 0; i < cardsToSeek; i++) {
            Card sought = matchingCards.remove(
                    ThreadLocalRandom.current().nextInt(matchingCards.size()));
            library.removeIf(card -> card.getId().equals(sought.getId()));
            gameData.addCardToHand(controllerId, sought);
            PerpetualCardCastCostSupport.remember(gameData, sought, reduction);
            soughtCards.add(sought);
        }
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, soughtCards);
    }
}
