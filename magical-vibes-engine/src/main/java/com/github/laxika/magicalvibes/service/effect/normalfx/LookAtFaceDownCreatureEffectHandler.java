package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreatureEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LookAtFaceDownCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtFaceDownCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var look = (LookAtFaceDownCreatureEffect) effect;
        for (Permanent creature : subjectResolutionService.permanents(gameData, entry, look, look.creatures())) {
            if (creature.isFaceDown() && gameQueryService.isCreature(gameData, creature)) {
                cardRevealService.lookAtFaceDownPermanent(gameData, entry.getControllerId(), creature);
            }
        }
    }
}
