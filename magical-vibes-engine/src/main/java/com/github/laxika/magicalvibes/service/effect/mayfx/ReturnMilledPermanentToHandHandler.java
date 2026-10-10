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
            int acceptedCount = ability.eventValue() + 1;
            if (acceptedCount >= marker.maxCount()) {
                removeOffersInGroup(gameData, groupId);
            } else {
                for (int i = 0; i < gameData.pendingMayAbilities.size(); i++) {
                    PendingMayAbility pending = gameData.pendingMayAbilities.get(i);
                    boolean sameGroup = pending.effects().stream()
                            .filter(ReturnMilledPermanentToHandEffect.class::isInstance)
                            .map(ReturnMilledPermanentToHandEffect.class::cast)
                            .anyMatch(candidate -> groupId.equals(candidate.groupId()));
                    if (sameGroup) {
                        gameData.pendingMayAbilities.set(i, pending.withEventValue(acceptedCount));
                    }
                }
            }

            UUID cardId = ability.sourceCard().getId();
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            boolean fromExile = false;
            if (card == null) {
                var exiled = gameData.findExiledCard(cardId);
                if (exiled != null) {
                    card = exiled.card();
                    ownerId = exiled.ownerId();
                    fromExile = true;
                }
            }
            if (card != null && ownerId != null
                    && predicateEvaluationService.matchesCardPredicate(
                    card, marker.filter(), cardId, gameData, ownerId)) {
                if (fromExile) {
                    gameData.exiledCards.removeIf(exiled -> exiled.card().getId().equals(cardId));
                    permanentRemovalService.addCardToHandFromGraveyard(gameData, null, ownerId, card);
                } else {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                    permanentRemovalService.addCardToHandFromGraveyard(gameData, ownerId, ownerId, card);
                }
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

    private void removeOffersInGroup(GameData gameData, UUID groupId) {
        gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                .filter(ReturnMilledPermanentToHandEffect.class::isInstance)
                .map(ReturnMilledPermanentToHandEffect.class::cast)
                .anyMatch(candidate -> groupId.equals(candidate.groupId())));
    }
}
