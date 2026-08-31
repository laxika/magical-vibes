package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandThenCreateFoodIfSquirrelEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles Cache Grab's mill, optional permanent return, and Squirrel condition. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MillControllerAndMayReturnMilledPermanentToHandThenCreateFoodIfSquirrelEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndMayReturnMilledPermanentToHandThenCreateFoodIfSquirrelEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndMayReturnMilledPermanentToHandThenCreateFoodIfSquirrelEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, controllerId, millEffect.count());
        List<Card> permanentCards = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, new CardIsPermanentPredicate(),
                        entry.getCard().getId(), gameData, controllerId))
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();

        if (permanentCards.isEmpty()) {
            if (controlsSquirrel(gameData, controllerId)) {
                resolveFood(gameData, entry, millEffect.foodEffect());
            }
            return;
        }

        UUID groupId = UUID.randomUUID();
        for (int i = permanentCards.size() - 1; i >= 0; i--) {
            Card card = permanentCards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect(
                            groupId, millEffect.foodEffect(), entry.getCard())),
                    "Put " + card.getName() + " into your hand?"));
        }
    }

    public boolean controlsSquirrel(GameData gameData, UUID controllerId) {
        List<com.github.laxika.magicalvibes.model.Permanent> battlefield =
                gameData.playerBattlefields.get(controllerId);
        return battlefield != null && battlefield.stream()
                .anyMatch(permanent -> gameQueryService.hasEffectiveSubtype(
                        gameData, permanent, CardSubtype.SQUIRREL));
    }

    public void resolveFood(GameData gameData, StackEntry entry, CardEffect foodEffect) {
        EffectHandler handler = effectHandlerRegistry.getHandler(foodEffect);
        if (handler != null) {
            handler.resolve(gameData, entry, foodEffect);
        } else {
            log.warn("No handler for Cache Grab follow-up effect: {}", foodEffect.getClass().getSimpleName());
        }
    }
}
