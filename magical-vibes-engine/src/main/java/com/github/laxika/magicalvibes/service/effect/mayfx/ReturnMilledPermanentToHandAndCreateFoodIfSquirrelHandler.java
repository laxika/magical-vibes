package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Handles Cache Grab's resolution-time card choice and conditional Food creation. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnMilledPermanentToHandAndCreateFoodIfSquirrelHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect marker = ability.effects().stream()
                .filter(ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect.class::isInstance)
                .map(ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect.class::cast)
                .findFirst()
                .orElseThrow();
        UUID groupId = marker.groupId();
        boolean returnedSquirrel = false;

        if (accepted) {
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .filter(ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect.class::isInstance)
                    .map(ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect.class::cast)
                    .anyMatch(candidate -> groupId.equals(candidate.groupId())));

            Card card = gameQueryService.findCardInGraveyardById(gameData, ability.sourceCard().getId());
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, ability.sourceCard().getId());
            if (card != null && ownerId != null
                    && predicateEvaluationService.matchesCardPredicate(
                    card, marker.filter(), ability.sourceCard().getId(), gameData, ownerId)) {
                returnedSquirrel = gameQueryService.cardHasSubtype(
                        card, CardSubtype.SQUIRREL, gameData, ownerId);
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                gameData.addCardToHand(ownerId, card);
            }

            if (returnedSquirrel || controlsSquirrel(gameData, ability.controllerId())) {
                resolveFood(gameData, ability, marker.sourceCard(), marker.foodEffect());
            }
        } else if (!hasPendingOfferForGroup(gameData, groupId)
                && controlsSquirrel(gameData, ability.controllerId())) {
            resolveFood(gameData, ability, marker.sourceCard(), marker.foodEffect());
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private boolean hasPendingOfferForGroup(GameData gameData, UUID groupId) {
        return gameData.pendingMayAbilities.stream()
                .flatMap(pending -> pending.effects().stream())
                .filter(ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect.class::isInstance)
                .map(ReturnMilledPermanentToHandAndCreateFoodIfSquirrelEffect.class::cast)
                .anyMatch(candidate -> groupId.equals(candidate.groupId()));
    }

    private boolean controlsSquirrel(GameData gameData, UUID controllerId) {
        List<com.github.laxika.magicalvibes.model.Permanent> battlefield =
                gameData.playerBattlefields.get(controllerId);
        return battlefield != null && battlefield.stream()
                .anyMatch(permanent -> gameQueryService.hasEffectiveSubtype(
                        gameData, permanent, CardSubtype.SQUIRREL));
    }

    private void resolveFood(GameData gameData, PendingMayAbility ability, Card sourceCard, CardEffect foodEffect) {
        EffectHandler handler = effectHandlerRegistry.getHandler(foodEffect);
        if (handler == null) {
            log.warn("No handler for Cache Grab follow-up effect: {}", foodEffect.getClass().getSimpleName());
            return;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                ability.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(foodEffect)),
                ability.targetCardId(),
                ability.sourcePermanentId());
        entry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        entry.setEventValue(ability.eventValue());
        handler.resolve(gameData, entry, foodEffect);
    }
}
