package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCreatureAndManifestEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Hamza's landfall seek by manifesting the exact creature card it finds. */
@Component
public class SeekCreatureAndManifestEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;
    private final ManifestService manifestService;

    public SeekCreatureAndManifestEffectHandler(
            PredicateEvaluationService predicateEvaluationService,
            TriggerCollectionService triggerCollectionService,
            ManifestService manifestService) {
        this.predicateEvaluationService = predicateEvaluationService;
        this.triggerCollectionService = triggerCollectionService;
        this.manifestService = manifestService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekCreatureAndManifestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        List<Card> matchingCards = library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, creature, entry.getCard() == null ? null : entry.getCard().getId(),
                        gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        Card selected = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.remove(selected);
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, List.of(selected));
        manifestService.manifestCard(gameData, controllerId, entry.getCard(), selected);
    }
}
