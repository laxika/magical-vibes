package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndPerpetuallyReduceSoughtCardEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Thought Rattle's threshold seek and perpetual cost reduction. */
@Component
public class SeekLibraryAndPerpetuallyReduceSoughtCardEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    public SeekLibraryAndPerpetuallyReduceSoughtCardEffectHandler(
            PredicateEvaluationService predicateEvaluationService) {
        this.predicateEvaluationService = predicateEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryAndPerpetuallyReduceSoughtCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekLibraryAndPerpetuallyReduceSoughtCardEffect seek =
                (SeekLibraryAndPerpetuallyReduceSoughtCardEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId))
                .toList());
        if (matchingCards.isEmpty()) {
            return;
        }

        Card sought = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.removeIf(card -> card.getId().equals(sought.getId()));

        Card modified = sought.createRuntimeCopy();
        modified.addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new Fixed(1)));
        gameData.addCardToHand(controllerId, modified);
    }
}
