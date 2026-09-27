package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect) effect;
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), e.cardFilter(), null, gameData, entry.getControllerId())) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualPowerToughnessChoice(
                entry.getControllerId(), validIndices,
                "Choose a creature card in your hand. It perpetually gets +"
                        + e.power() + "/+" + e.toughness() + " and costs {" + e.costReduction()
                        + "} less to cast.",
                e.power(), e.toughness(), java.util.Set.of(), e.costReduction()));
    }
}
