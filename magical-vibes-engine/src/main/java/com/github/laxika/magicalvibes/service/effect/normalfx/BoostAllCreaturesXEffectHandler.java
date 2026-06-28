package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesXEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostAllCreaturesXEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostAllCreaturesXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BoostAllCreaturesXEffect) effect;
        int xValue = entry.getXValue();
        int powerBoost = e.powerMultiplier() * xValue;
        int toughnessBoost = e.toughnessMultiplier() * xValue;

        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (e.filter() == null
                        || gameQueryService.matchesPermanentPredicate(gameData, permanent, e.filter()))) {
                permanent.setPowerModifier(permanent.getPowerModifier() + powerBoost);
                permanent.setToughnessModifier(permanent.getToughnessModifier() + toughnessBoost);
                count[0]++;
            }
        });

        String logEntry = String.format("%s gives %+d/%+d to %d creature(s) until end of turn.",
                entry.getCard().getName(), powerBoost, toughnessBoost, count[0]);
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gives {}/{} to {} creatures", gameData.id, entry.getCard().getName(), powerBoost, toughnessBoost, count[0]);
    }
}
