package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AuraSwapEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves Aura swap by offering eligible Aura cards in hand and returning the source Aura when one
 * is chosen. The source and its enchanted permanent must still exist when the choice is made.
 */
@Component
@RequiredArgsConstructor
public class AuraSwapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AuraAttachmentService auraAttachmentService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AuraSwapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.getCard().isAura() || !source.isAttached()) {
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
                if (card.isAura() && !card.isEnchantPlayer()
                        && auraAttachmentService.canEnchant(gameData, card, controllerId, host)) {
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
                "You may exchange this Aura with an Aura card from your hand.",
                host.getId(),
                null,
                source.getId());
    }
}
