package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleOtherOwnCreaturesPowerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleOtherOwnCreaturesPowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleOtherOwnCreaturesPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        UUID sourceId = entry.getSourcePermanentId();
        int count = 0;
        for (Permanent permanent : battlefield) {
            if ((sourceId != null && sourceId.equals(permanent.getId()))
                    || !gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            int currentPower = gameQueryService.getEffectivePower(gameData, permanent);
            permanent.setPowerModifier(permanent.getPowerModifier() + currentPower);
            count++;
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" doubles the power of " + count + " other creature(s) until end of turn.")
                .build());

        log.info("Game {} - {} doubles the power of {} other creatures", gameData.id,
                entry.getCard().getName(), count);
    }
}
