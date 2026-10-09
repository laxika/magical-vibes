package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Records a perpetual triggered-ability grant on one random matching card in the controller's hand. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> matchingCards = gameData.playerHands.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> grant.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        card, grant.filter(), null, gameData, controllerId))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        Card selected = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        gameData.perpetualTriggeredAbilityGrants.compute(selected.getId(), (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            CardEffect grantedAbility = grant.grantedEffects().size() == 1
                    ? grant.grantedEffects().getFirst()
                    : new com.github.laxika.magicalvibes.model.effect.SequenceEffect(grant.grantedEffects());
            updated.computeIfAbsent(grant.slot(), ignoredSlot -> new ArrayList<>()).add(grantedAbility);
            updated.replaceAll((slot, effects) -> List.copyOf(effects));
            return Map.copyOf(updated);
        });
    }
}
