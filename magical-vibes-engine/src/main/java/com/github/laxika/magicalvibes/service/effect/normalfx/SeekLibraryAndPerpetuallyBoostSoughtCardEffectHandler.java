package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndPerpetuallyBoostSoughtCardEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a seek that gives the exact sought card a perpetual power/toughness boost. */
@Component
@RequiredArgsConstructor
public class SeekLibraryAndPerpetuallyBoostSoughtCardEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryAndPerpetuallyBoostSoughtCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var seek = (SeekLibraryAndPerpetuallyBoostSoughtCardEffect) effect;
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
        gameData.addCardToHand(controllerId, sought);
        PerpetualCardPowerToughnessSupport.remember(
                gameData, sought, seek.powerBoost(), seek.toughnessBoost());
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, List.of(sought));
    }
}
