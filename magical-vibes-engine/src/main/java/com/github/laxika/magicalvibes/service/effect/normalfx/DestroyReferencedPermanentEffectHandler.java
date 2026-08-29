package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
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
            case RETURNED -> findPermanentByCardId(gameData, entry.getTargetId());
        };
        if (referenced == null) {
            return;
        }

        destructionSupport.destroyBatch(
                gameData, List.of(referenced), entry.getCard().getName(), e.cannotBeRegenerated());
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .filter(permanent -> cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId())))
                .findFirst()
                .orElse(null);
    }

    private Permanent findAttached(GameData gameData, StackEntry entry) {
        Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
        if (source == null || !source.isAttached()) {
            return null;
        }
        return gameQueryService.findPermanentById(gameData, source.getAttachedTo());
    }
}
