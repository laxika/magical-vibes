package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyReferencedPermanentEffect}: resolves the referenced permanent, then hands
 * it to {@link DestructionSupport#tryDestroyAndLog}, which owns indestructible, regeneration and
 * the death bookkeeping. A reference that no longer resolves is a silent no-op — none of the three
 * references targets, so there is nothing to fizzle.
 */
@Component
@RequiredArgsConstructor
public class DestroyReferencedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyReferencedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyReferencedPermanentEffect e = (DestroyReferencedPermanentEffect) effect;

        Permanent referenced = switch (e.reference()) {
            case SOURCE -> findPermanent(gameData, entry.getSourcePermanentId());
            case ATTACHED -> findAttached(gameData, entry);
            case TRIGGERING -> findPermanent(gameData, entry.getTriggeringPermanentId());
        };
        if (referenced == null) {
            return;
        }

        destructionSupport.tryDestroyAndLog(gameData, referenced, entry.getCard().getName(), e.cannotBeRegenerated());
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findAttached(GameData gameData, StackEntry entry) {
        Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
        if (source == null || !source.isAttached()) {
            return null;
        }
        return gameQueryService.findPermanentById(gameData, source.getAttachedTo());
    }
}
