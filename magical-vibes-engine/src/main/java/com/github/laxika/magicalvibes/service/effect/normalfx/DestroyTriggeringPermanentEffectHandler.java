package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Destroys the permanent whose event produced the trigger (Suleiman's Legacy destroying an entering
 * Djinn or Efreet). Reads {@code StackEntry.triggeringPermanentId}; no-op if it has left.
 */
@Component
@RequiredArgsConstructor
public class DestroyTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTriggeringPermanentId() == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (target == null) {
            return;
        }
        boolean cannotRegenerate = ((DestroyTriggeringPermanentEffect) effect).cannotBeRegenerated();
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), cannotRegenerate);
    }
}
