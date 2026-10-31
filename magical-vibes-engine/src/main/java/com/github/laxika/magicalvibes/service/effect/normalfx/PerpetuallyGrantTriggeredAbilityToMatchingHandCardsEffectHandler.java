package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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

            grant.grantedEffects().forEach(granted -> gameData.grantPerpetualAbility(card.getId(), grant.slot(), granted));
        }
    }
}
