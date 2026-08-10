package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquippedCreatureAndCombatOpponentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link DestroyEquippedCreatureAndCombatOpponentEffect} as one batch so both creatures
 * are destroyed together and regeneration or indestructibility is handled consistently.
 */
@Component
@RequiredArgsConstructor
public class DestroyEquippedCreatureAndCombatOpponentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyEquippedCreatureAndCombatOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> permanents = new ArrayList<>(2);
        addIfPresent(gameData, permanents, entry.getTriggeringPermanentId());
        addIfPresent(gameData, permanents, entry.getTargetId());
        destructionSupport.destroyBatch(gameData, permanents, entry.getCard().getName(), false);
    }

    private void addIfPresent(GameData gameData, List<Permanent> permanents, UUID permanentId) {
        if (permanentId == null) {
            return;
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent != null && !permanents.contains(permanent)) {
            permanents.add(permanent);
        }
    }
}
