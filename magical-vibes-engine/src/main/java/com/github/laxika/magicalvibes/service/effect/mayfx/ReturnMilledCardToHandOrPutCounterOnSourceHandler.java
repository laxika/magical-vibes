package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledCardToHandOrPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutCountersOnSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles grouped offers created by mill abilities that return cards or put a counter on the source. */
@Component
@RequiredArgsConstructor
public class ReturnMilledCardToHandOrPutCounterOnSourceHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PutCountersOnSourceEffectHandler putCountersOnSourceEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnMilledCardToHandOrPutCounterOnSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        ReturnMilledCardToHandOrPutCounterOnSourceEffect marker = ability.effects().stream()
                .filter(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::isInstance)
                .map(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::cast)
                .findFirst().orElseThrow();
        UUID groupId = marker.groupId();

        if (accepted) {
            int acceptedCount = ability.eventValue() + 1;
            if (acceptedCount >= marker.maxCount()) {
                removeOffersInGroup(gameData, groupId);
            } else {
                for (int i = 0; i < gameData.pendingMayAbilities.size(); i++) {
                    PendingMayAbility pending = gameData.pendingMayAbilities.get(i);
                    boolean sameGroup = pending.effects().stream()
                            .filter(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::isInstance)
                            .map(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::cast)
                            .anyMatch(candidate -> groupId.equals(candidate.groupId()));
                    if (sameGroup) {
                        gameData.pendingMayAbilities.set(i, pending.withEventValue(acceptedCount));
                    }
                }
            }
            Card card = gameQueryService.findCardInGraveyardById(gameData, ability.targetCardId());
            UUID ownerId = card == null ? null : gameQueryService.findGraveyardOwnerById(gameData, card.getId());
            if (card != null && ownerId != null) {
                permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                permanentRemovalService.addCardToHandFromGraveyard(gameData, ownerId, ownerId, card);
            } else if (acceptedCount == 1 && gameData.pendingMayAbilities.stream().noneMatch(pending ->
                    pending.effects().stream()
                            .filter(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::isInstance)
                            .map(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::cast)
                            .anyMatch(candidate -> groupId.equals(candidate.groupId())))) {
                putCounterOnSource(gameData, ability);
            }
        } else if (gameData.pendingMayAbilities.stream().noneMatch(pending ->
                pending.effects().stream()
                        .filter(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::isInstance)
                        .map(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::cast)
                        .anyMatch(candidate -> groupId.equals(candidate.groupId())))
                && ability.eventValue() == 0) {
            putCounterOnSource(gameData, ability);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void removeOffersInGroup(GameData gameData, UUID groupId) {
        gameData.pendingMayAbilities.removeIf(pending -> pending.effects().stream()
                .filter(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::isInstance)
                .map(ReturnMilledCardToHandOrPutCounterOnSourceEffect.class::cast)
                .anyMatch(marker -> groupId.equals(marker.groupId())));
    }

    private void putCounterOnSource(GameData gameData, PendingMayAbility ability) {
        if (ability.sourcePermanentId() == null) {
            return;
        }

        PutCountersOnSourceEffect counterEffect = new PutCountersOnSourceEffect(1, 1, 1);
        StackEntry counterEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                ability.controllerId(),
                ability.sourceCard().getName() + "'s ability",
                List.of(counterEffect),
                0,
                ability.sourcePermanentId());
        putCountersOnSourceEffectHandler.resolve(gameData, counterEntry, counterEffect);
    }
}
