package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreatureEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LookAtFaceDownCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtFaceDownCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !target.isFaceDown() || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        cardRevealService.lookAtFaceDownPermanent(gameData, entry.getControllerId(), target);
    }
}
