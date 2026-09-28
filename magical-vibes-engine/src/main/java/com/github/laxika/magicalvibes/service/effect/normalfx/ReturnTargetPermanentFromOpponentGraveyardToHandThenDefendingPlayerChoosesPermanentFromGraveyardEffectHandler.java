package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ReturnCardFromGraveyardEffectHandler returnCardFromGraveyardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var orderedEffect =
                (ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect) effect;
        UUID targetCardId = entry.getTargetCardIdsForEffect(effect).stream().findFirst().orElse(null);
        if (targetCardId == null) {
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID targetOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null || targetOwnerId == null) {
            return;
        }

        ReturnCardFromGraveyardEffect returnToHand = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(orderedEffect.filter())
                .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                .targetGraveyard(true)
                .upTo(true)
                .build();
        returnCardFromGraveyardEffectHandler.resolve(gameData, entry, returnToHand);

        boolean returned = gameData.playerHands.getOrDefault(targetOwnerId, List.of()).stream()
                .anyMatch(card -> card.getId().equals(targetCardId));
        if (!returned) {
            return;
        }

        // The shared combat graveyard-target queue carries the selected card id but not the
        // damaged-player context on its resulting stack entry. Preserve that player for the
        // resolution-time choice that follows the successful return.
        entry.setTargetId(targetOwnerId);
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException(
                    "ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect is not part of its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect.withoutRiders(orderedEffect.filter())));
    }
}
