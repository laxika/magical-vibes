package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetualAnyManaTypeToCastSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardAndConjureOpponentGraveyardDuplicateEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Nantuko Slicer's return and kicked duplicate targets. */
@Component
@RequiredArgsConstructor
public class ReturnCardAndConjureOpponentGraveyardDuplicateEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardAndConjureOpponentGraveyardDuplicateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Integer> groupSizes = entry.getTargetCardGroupSizes();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (groupSizes.size() != 2 || targetCardIds == null) {
            return;
        }

        int firstGroupSize = groupSizes.getFirst();
        int secondGroupSize = groupSizes.get(1);
        if (firstGroupSize < 0 || secondGroupSize < 0
                || firstGroupSize + secondGroupSize > targetCardIds.size()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<UUID> returnedCardIds = targetCardIds.subList(0, firstGroupSize).stream()
                .filter(cardId -> controllerId.equals(gameQueryService.findGraveyardOwnerById(gameData, cardId)))
                .toList();
        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry, returnedCardIds,
                (graveyard, card) -> gameData.addCardToHand(controllerId, card),
                " returns ", " from graveyard to hand.");

        int duplicateOffset = firstGroupSize;
        for (UUID cardId : targetCardIds.subList(duplicateOffset, duplicateOffset + secondGroupSize)) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (sourceCard == null || graveyardOwnerId == null || controllerId.equals(graveyardOwnerId)) {
                continue;
            }

            Card duplicate = sourceCard.createRuntimeCopyWithNewId();
            duplicate.setOwnerId(controllerId);
            duplicate.setToken(true);
            duplicate.setTokenCard(true);
            duplicate.addEffect(EffectSlot.STATIC, new PerpetualAnyManaTypeToCastSelfEffect());
            duplicate.freeze();
            gameData.addCardToHand(controllerId, duplicate);
            gameLogService.append(gameData, GameLog.textCardText(
                    "A duplicate of ", duplicate, " is conjured into your hand."));
        }
    }
}
