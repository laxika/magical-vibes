package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandToPerpetuallyGrantStaticEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Begins an optional hand-card choice for a perpetual static-effect grant. */
@Component
@RequiredArgsConstructor
public class ChooseCardFromHandToPerpetuallyGrantStaticEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardFromHandToPerpetuallyGrantStaticEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (ChooseCardFromHandToPerpetuallyGrantStaticEffect) effect;
        List<Card> hand = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of());
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), choiceEffect.cardFilter(), null, gameData, entry.getControllerId())) {
                validIndices.add(i);
            }
        }

        if (validIndices.isEmpty()) {
            resolveFallback(gameData, entry, choiceEffect.fallbackEffect());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PerpetualStaticEffectCardChoice(
                entry.getControllerId(), validIndices,
                "Choose a creature card in your hand to give an additional shield counter to, or decline.",
                choiceEffect.staticEffect(), choiceEffect.cardFilter(), choiceEffect.fallbackEffect()));
    }

    public void resolveFallback(GameData gameData, StackEntry entry, CardEffect fallbackEffect) {
        if (fallbackEffect == null) {
            return;
        }
        EffectHandler handler = effectHandlerRegistry.getHandler(fallbackEffect);
        if (handler == null) {
            throw new IllegalStateException("No handler for fallback effect: "
                    + fallbackEffect.getClass().getName());
        }
        handler.resolve(gameData, entry, fallbackEffect);
    }
}
