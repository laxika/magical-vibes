package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualNoncombatDamageEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChooseCardFromHandAndApplyPerpetualNoncombatDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromHandAndApplyPerpetualNoncombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChooseCardFromHandAndApplyPerpetualNoncombatDamageEffect) effect;
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
                "Choose an instant or sorcery card in your hand. It perpetually deals +"
                        + e.amount() + " noncombat damage.",
                0, 0, java.util.Set.of(), 0, e.amount()));
    }
}
