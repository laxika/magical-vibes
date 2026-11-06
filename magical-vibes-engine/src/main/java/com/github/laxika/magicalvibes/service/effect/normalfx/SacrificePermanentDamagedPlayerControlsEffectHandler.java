package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificePermanentDamagedPlayerControlsEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentDamagedPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The target is "that player"'s permanent, checked against the entry's triggering player as the ability
        // resolves; its controller sacrifices it.
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId != null) {
            destructionSupport.sacrificeAndLog(gameData, target, controllerId);
        }
    }
}
