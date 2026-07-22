package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleAllOwnCreaturesPowerToughnessEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleAllOwnCreaturesPowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleAllOwnCreaturesPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            int currentPower = gameQueryService.getEffectivePower(gameData, permanent);
            int currentToughness = gameQueryService.getEffectiveToughness(gameData, permanent);
            permanent.setPowerModifier(permanent.getPowerModifier() + currentPower);
            permanent.setToughnessModifier(permanent.getToughnessModifier() + currentToughness);
            count++;
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" doubles the power and toughness of " + count + " creature(s) until end of turn.")
                .build());

        log.info("Game {} - {} doubles P/T of {} creatures", gameData.id, entry.getCard().getName(), count);
    }
}
