package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveDelayedPermanentActionEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies the original scheduled action only when its delayed ability resolves. */
@Component
@RequiredArgsConstructor
public class ResolveDelayedPermanentActionEffectHandler implements NormalEffectHandlerBean {
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ResolveDelayedPermanentActionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        permanentRemovalService.resolveDelayedPermanentAction(gameData,
                ((ResolveDelayedPermanentActionEffect) effect).action());
    }
}
