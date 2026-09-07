package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyLinkedPermanentEffect}: destroys the permanent the source is linked to.
 * The id comes from the effect when the collector baked it in (the source has already left the
 * battlefield), otherwise from the still-present source permanent's link.
 */
@Component
@RequiredArgsConstructor
public class DestroyLinkedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyLinkedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyLinkedPermanentEffect) effect;

        UUID linkedId = destroy.linkedPermanentId();
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (linkedId == null) {
            linkedId = source == null ? null : source.getChosenPermanentId();
        }
        if (source != null && linkedId != null && linkedId.equals(source.getChosenPermanentId())) {
            source.setChosenPermanentId(null);
        }
        if (linkedId == null) {
            return;
        }

        Permanent linked = gameQueryService.findPermanentById(gameData, linkedId);
        if (linked == null) {
            return;
        }
        destructionSupport.tryDestroyAndLog(gameData, linked, entry.getCard().getName(),
                destroy.cannotBeRegenerated());
    }
}
