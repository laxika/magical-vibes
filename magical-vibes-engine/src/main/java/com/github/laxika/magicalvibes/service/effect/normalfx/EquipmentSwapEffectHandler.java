package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EquipmentSwapEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Equipment swap by offering eligible Equipment cards in hand and returning the source
 * Equipment when one is chosen. The source and its equipped creature must still exist when the
 * choice is made.
 */
@Component
@RequiredArgsConstructor
public class EquipmentSwapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EquipmentSwapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                || !source.isAttached()) {
            return;
        }

        UUID ownerId = source.getCard().getOwnerId();
        if (ownerId != null && !ownerId.equals(controllerId)) {
            return;
        }

        Permanent host = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        if (host == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (card.getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && equipSupport.canAttachEquipment(gameData, new Permanent(card), host)) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            return;
        }

        playerInputService.beginTargetedCardChoice(
                gameData,
                controllerId,
                validIndices,
                "You may exchange this Equipment with an Equipment card from your hand.",
                host.getId(),
                null,
                source.getId());
    }
}
