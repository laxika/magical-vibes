package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PlayerCounterType;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnControllerEffect;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * "Double the number of each kind of counter you have": the controller is given as many of each kind as they already
 * have (CR 701.10e), so the player lock and the counter-placement replacements apply to each kind.
 */
@Component
@RequiredArgsConstructor
public class DoubleCountersOnControllerEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleCountersOnControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        int multiplier = MaroGoneNutsSupport.apply(gameData, effect, 2);
        String sourceName = entry.getCard().getName();
        for (PlayerCounterType kind : PlayerCounterType.values()) {
            int current = gameData.playerCounterCount(playerId, kind);
            if (current > 0) {
                lifeSupport.givePlayerCounters(gameData, playerId, kind,
                        SaturatingMath.subtract(SaturatingMath.multiply(current, multiplier), current), sourceName,
                        playerId);
            }
        }
    }
}
