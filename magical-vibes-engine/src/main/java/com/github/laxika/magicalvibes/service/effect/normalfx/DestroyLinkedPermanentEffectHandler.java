package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves {@link DestroyLinkedPermanentEffect}: destroys the predetermined permanent if it is still there. */
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

        Permanent linked = gameQueryService.findPermanentById(gameData, destroy.linkedPermanentId());
        if (linked == null) {
            return;
        }
        destructionSupport.tryDestroyAndLog(gameData, linked, entry.getCard().getName(),
                destroy.cannotBeRegenerated());
    }
}
