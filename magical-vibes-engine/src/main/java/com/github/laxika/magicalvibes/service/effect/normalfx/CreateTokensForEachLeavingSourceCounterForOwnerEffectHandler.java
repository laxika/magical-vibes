package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachLeavingSourceCounterForOwnerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a self-leaves trigger that creates tokens for the source card's owner. */
@Component
@RequiredArgsConstructor
public class CreateTokensForEachLeavingSourceCounterForOwnerEffectHandler
        implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensForEachLeavingSourceCounterForOwnerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensForEachLeavingSourceCounterForOwnerEffect tokenEffect =
                (CreateTokensForEachLeavingSourceCounterForOwnerEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null) {
            return;
        }

        int amount = source.getCounterCount(tokenEffect.counterType());
        UUID ownerId = source.getCard().getOwnerId();
        if (amount <= 0 || ownerId == null || !gameData.playerIds.contains(ownerId)) {
            return;
        }

        createTokenEffectHandler.resolveForController(
                gameData, entry, tokenEffect.tokenTemplate().withAmount(amount), ownerId);
    }
}
