package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetThenUntapAnotherTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapTargetThenUntapAnotherTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapTargetThenUntapAnotherTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.getTargetIds();

        Permanent toTap = targetIds.isEmpty() ? null : gameQueryService.findPermanentById(gameData, targetIds.get(0));
        if (toTap != null) {
            tapUntapSupport.tapPermanent(gameData, toTap);
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " taps ", toTap.getCard(), "."));
            log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), toTap.getCard().getName());
        }

        Permanent toUntap = targetIds.size() < 2 ? null : gameQueryService.findPermanentById(gameData, targetIds.get(1));
        if (toUntap != null) {
            tapUntapSupport.untapPermanent(gameData, toUntap);
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " untaps ", toUntap.getCard(), "."));
            log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), toUntap.getCard().getName());
        }
    }
}
