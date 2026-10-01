package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Records perpetual triggered-ability grants on matching cards in the controller's hand. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        for (Card card : gameData.playerHands.getOrDefault(controllerId, List.of())) {
            if (grant.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                    card, grant.filter(), null, gameData, controllerId)) {
                continue;
            }

            gameData.perpetualTriggeredAbilityGrants.compute(card.getId(), (ignored, existing) -> {
                Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
                if (existing != null) {
                    existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
                }
                updated.computeIfAbsent(grant.slot(), ignoredSlot -> new ArrayList<>())
                        .addAll(grant.grantedEffects());
                updated.replaceAll((slot, effects) -> List.copyOf(effects));
                return Map.copyOf(updated);
            });
        }
    }
}
