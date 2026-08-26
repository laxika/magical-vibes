package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Handles one of Oko's resolution-time offers to return a milled permanent card to hand. */
@Component
@RequiredArgsConstructor
public class ReturnMilledPermanentToHandHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final InputCompletionService inputCompletionService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnMilledPermanentToHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ReturnMilledPermanentToHandEffect marker = ability.effects().stream()
                .filter(ReturnMilledPermanentToHandEffect.class::isInstance)
                .map(ReturnMilledPermanentToHandEffect.class::cast)
                .findFirst()
                .orElseThrow();
        UUID groupId = marker.groupId();

        if (accepted) {
            gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                    .filter(ReturnMilledPermanentToHandEffect.class::isInstance)
                    .map(ReturnMilledPermanentToHandEffect.class::cast)
                    .anyMatch(candidate -> groupId.equals(candidate.groupId())));

            Card card = gameQueryService.findCardInGraveyardById(gameData, ability.sourceCard().getId());
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, ability.sourceCard().getId());
            if (card != null && ownerId != null
                    && predicateEvaluationService.matchesCardPredicate(
                    card, marker.filter(), ability.sourceCard().getId(), gameData, ownerId)) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                gameData.addCardToHand(ownerId, card);
                if (marker.bonusFilter() != null
                        && predicateEvaluationService.matchesCardPredicate(
                        card, marker.bonusFilter(), ability.sourceCard().getId(), gameData, ownerId)) {
                    lifeSupport.applyGainLife(gameData, ability.controllerId(), marker.bonusLife(),
                            null, ability.sourceCard(), StackEntryType.TRIGGERED_ABILITY);
                }
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
