package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerGainsLifeEqualToManaValueEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link TargetSpellControllerGainsLifeEqualToManaValueEffect} (Illumination). Reads the
 * mana value of the targeted spell — still on the stack because this resolves before the counter —
 * and gives that much life to the spell's controller.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetSpellControllerGainsLifeEqualToManaValueEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetSpellControllerGainsLifeEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                int manaValue = se.getCard().getManaValue() + se.getXValue();
                if (manaValue > 0) {
                    lifeSupport.applyGainLife(gameData, se.getControllerId(), manaValue,
                            entry.getCard().getName());
                }
                return;
            }
        }
        log.info("Game {} - Target spell no longer on stack for life gain", gameData.id);
    }
}
