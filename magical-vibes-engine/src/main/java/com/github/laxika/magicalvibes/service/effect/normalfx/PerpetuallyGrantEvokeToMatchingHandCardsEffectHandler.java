package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantEvokeToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves perpetual Evoke grants to matching cards currently in hand. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantEvokeToMatchingHandCardsEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantEvokeToMatchingHandCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantEvokeToMatchingHandCardsEffect) effect;
        AlternateHandCast evoke = new AlternateHandCast(List.of(new ExileCardsFromHandCastingCost(
                grant.exiledCardFilter(), grant.exiledCardLabel())));

        for (Card card : gameData.playerHands.getOrDefault(entry.getControllerId(), List.of())) {
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, grant.cardFilter(), null, gameData, entry.getControllerId())) {
                continue;
            }
            gameData.perpetualEvokeAlternateCasts.compute(card.getId(), (ignored, existing) -> {
                List<AlternateHandCast> updated = new ArrayList<>(existing == null ? List.of() : existing);
                if (!updated.contains(evoke)) {
                    updated.add(evoke);
                }
                return List.copyOf(updated);
            });
        }
    }
}
