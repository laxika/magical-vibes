package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryAndPerpetuallyModifySoughtCardEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a seek that gives the exact sought card perpetual modifications. */
@Component
@RequiredArgsConstructor
public class SeekLibraryAndPerpetuallyModifySoughtCardEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryAndPerpetuallyModifySoughtCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var seek = (SeekLibraryAndPerpetuallyModifySoughtCardEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> card.getManaValue() <= seek.maxManaValue())
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId))
                .toList());
        if (matchingCards.isEmpty()) {
            return;
        }

        Card sought = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.removeIf(card -> card.getId().equals(sought.getId()));
        gameData.addCardToHand(controllerId, sought);

        UUID cardId = sought.getId();
        if (!seek.keywords().isEmpty()) {
            gameData.perpetualCardKeywords.merge(cardId, seek.keywords(),
                    SeekLibraryAndPerpetuallyModifySoughtCardEffectHandler::mergeKeywords);
        }
        if (seek.genericCastCostReduction() > 0) {
            gameData.perpetualCardCastCostReductions.merge(
                    cardId, seek.genericCastCostReduction(), Integer::sum);
        }
        if (seek.triggeredAbility() != null) {
            addPerpetualTriggeredAbility(gameData, cardId,
                    seek.triggeredAbilitySlot(), seek.triggeredAbility());
        }

        triggerCollectionService.checkSeekTriggers(gameData, controllerId, List.of(sought));
    }

    private static Set<Keyword> mergeKeywords(Set<Keyword> existing, Set<Keyword> added) {
        EnumSet<Keyword> merged = EnumSet.noneOf(Keyword.class);
        merged.addAll(existing);
        merged.addAll(added);
        return Set.copyOf(merged);
    }

    private static void addPerpetualTriggeredAbility(GameData gameData, UUID cardId,
                                                      EffectSlot slot, CardEffect ability) {
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new java.util.EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((existingSlot, effects) ->
                        updated.put(existingSlot, new ArrayList<>(effects)));
            }
            List<CardEffect> effects = updated.computeIfAbsent(slot, ignoredSlot -> new ArrayList<>());
            if (!effects.contains(ability)) {
                effects.add(ability);
            }
            updated.replaceAll((existingSlot, slotEffects) -> List.copyOf(slotEffects));
            return Map.copyOf(updated);
        });
    }
}
