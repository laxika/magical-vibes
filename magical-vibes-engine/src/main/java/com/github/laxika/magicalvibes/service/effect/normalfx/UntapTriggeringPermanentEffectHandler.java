package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UntapTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringPermanentId = entry.getTriggeringPermanentId();
        Permanent triggeringPermanent = gameQueryService.findPermanentById(gameData, triggeringPermanentId);
        if (triggeringPermanent == null) {
            return;
        }

        tapUntapSupport.untapPermanent(gameData, triggeringPermanent);
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " untaps ",
                triggeringPermanent.getCard(), "."));
        log.info("Game {} - {} untaps triggering permanent {}", gameData.id,
                entry.getCard().getName(), triggeringPermanent.getCard().getName());
    }
}
