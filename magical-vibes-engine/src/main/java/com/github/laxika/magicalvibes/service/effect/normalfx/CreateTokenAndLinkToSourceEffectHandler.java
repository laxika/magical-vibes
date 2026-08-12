package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndLinkToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates a token using the normal token pipeline and records the source/token relationship needed
 * by Stangg-style paired leaves-the-battlefield abilities.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenAndLinkToSourceEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenAndLinkToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var linked = (CreateTokenAndLinkToSourceEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

        CreateTokenEffect token = linked.token();
        if (source != null) {
            var tokenEffects = new EnumMap<EffectSlot, CardEffect>(EffectSlot.class);
            if (token.tokenEffects() != null) {
                tokenEffects.putAll(token.tokenEffects());
            }
            tokenEffects.put(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                    new RemoveLinkedPermanentEffect(RemoveLinkedPermanentEffect.Mode.SACRIFICE));
            token = token.withTokenEffects(tokenEffects);
        }

        int firstCreatedIndex = entry.getCreatedPermanentIds().size();
        createTokenEffectHandler.resolve(gameData, entry, token);
        if (source == null || entry.getCreatedPermanentIds().size() == firstCreatedIndex) {
            return;
        }

        List<UUID> createdIds = new ArrayList<>(entry.getCreatedPermanentIds()
                .subList(firstCreatedIndex, entry.getCreatedPermanentIds().size()));
        gameData.sourceCreatedTokens
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .addAll(createdIds);
        for (UUID createdId : createdIds) {
            Permanent tokenPermanent = gameQueryService.findPermanentById(gameData, createdId);
            if (tokenPermanent != null) {
                tokenPermanent.setChosenPermanentId(source.getId());
            }
        }
    }
}
