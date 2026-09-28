package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrainLifeFromDyingCreatureOwnerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a life drain from the owner of the creature that caused an ally-death trigger. */
@Component
@RequiredArgsConstructor
public class DrainLifeFromDyingCreatureOwnerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrainLifeFromDyingCreatureOwnerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var drain = (DrainLifeFromDyingCreatureOwnerEffect) effect;
        if (drain.amount() <= 0) return;

        UUID dyingCardId = drain.dyingCardId() != null
                ? drain.dyingCardId() : entry.getTriggeringPermanentId();
        Card dyingCard = entry.getTriggeringPermanentId() == null
                ? null : entry.lastKnownPermanentCard(entry.getTriggeringPermanentId());
        if (dyingCardId == null) return;

        UUID ownerId = dyingCard == null ? null : dyingCard.getOwnerId();
        if (ownerId == null) {
            ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        }
        if (ownerId == null || !gameData.playerIds.contains(ownerId)) return;

        lifeSupport.applyLifeLoss(gameData, ownerId, drain.amount(), entry.getCard().getName());
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), drain.amount());
    }
}
