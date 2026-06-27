package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureXEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostTargetCreatureXEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreatureXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostTargetCreatureXEffect) effect;
        int xValue = entry.getXValue();
        int powerBoost = e.powerMultiplier() * xValue;
        int toughnessBoost = e.toughnessMultiplier() * xValue;

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setPowerModifier(target.getPowerModifier() + powerBoost);
        target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);

        String logEntry = String.format("%s gets %+d/%+d until end of turn.",
                target.getCard().getName(), powerBoost, toughnessBoost);
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets {}/{}", gameData.id, target.getCard().getName(), powerBoost, toughnessBoost);
    }
}
