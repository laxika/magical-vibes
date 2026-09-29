package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantActivatedAbilityToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Records a perpetual activated-ability grant for each matching card currently in hand. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantActivatedAbilityToMatchingHandCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantActivatedAbilityToMatchingHandCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantActivatedAbilityToMatchingHandCardsEffect) effect;
        for (Card card : gameData.playerHands.getOrDefault(entry.getControllerId(), List.of())) {
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, grant.cardFilter(), null, gameData, entry.getControllerId())) {
                continue;
            }
            ActivatedAbility ability = grant.ability();
            gameData.perpetualActivatedAbilities.compute(card.getId(), (ignored, existing) -> {
                List<ActivatedAbility> updated = new ArrayList<>(existing == null ? List.of() : existing);
                if (!updated.contains(ability)) {
                    updated.add(ability);
                }
                return List.copyOf(updated);
            });
        }
    }
}
