package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldForOwnerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a hand-to-battlefield choice for the source card's owner. */
@Component
@RequiredArgsConstructor
public class PutCardToBattlefieldForOwnerEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardToBattlefieldForOwnerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardToBattlefieldForOwnerEffect) effect;
        UUID ownerId = entry.getCard() != null && entry.getCard().getOwnerId() != null
                ? entry.getCard().getOwnerId() : entry.getControllerId();
        if (ownerId == null || !gameData.playerIds.contains(ownerId)) {
            return;
        }

        playerInteractionSupport.applyPutCardToBattlefield(
                gameData,
                ownerId,
                new PutCardToBattlefieldEffect(e.predicate(), e.label(), e.enterTapped()),
                entry.getXValue(),
                entry.getEventValue(),
                null,
                entry.getCard() == null ? null : entry.getCard().getId(),
                null,
                null,
                null,
                ignored -> true,
                entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot());
    }
}
