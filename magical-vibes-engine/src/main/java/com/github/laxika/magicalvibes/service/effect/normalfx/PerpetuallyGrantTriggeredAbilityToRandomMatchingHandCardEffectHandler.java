package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
        CardEffect grantedAbility = grant.grantedEffects().size() == 1
                ? grant.grantedEffects().getFirst()
                : new com.github.laxika.magicalvibes.model.effect.SequenceEffect(grant.grantedEffects());
        gameData.grantPerpetualAbility(selected.getId(), grant.slot(), grantedAbility);
    }
}
