package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturePreparedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeTargetCreaturePreparedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PreparedSupport preparedSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetCreaturePreparedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
        preparedSupport.preparePermanent(gameData, target, controllerId);
    }
}
