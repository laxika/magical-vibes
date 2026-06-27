package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetSpellControllerLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetSpellControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetSpellControllerLosesLifeEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                lifeSupport.applyLifeLoss(gameData, se.getControllerId(), e.amount(), entry.getCard().getName());
                return;
            }
        }
        // Target spell already left the stack (e.g. was countered by earlier effect on the same spell)
        log.info("Game {} - Target spell no longer on stack for life loss", gameData.id);
    }
}
