package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyBlockedCreatureAndSelfEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyBlockedCreatureAndSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        java.util.List<Permanent> toDestroy = new java.util.ArrayList<>();
        if (attacker != null) toDestroy.add(attacker);
        if (self != null) toDestroy.add(self);
        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
    }
}
