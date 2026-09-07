package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CantBlockThisCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantBlockThisCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CantBlockThisCombatEffect cantBlockEffect = (CantBlockThisCombatEffect) effect;
        UUID affectedPermanentId = cantBlockEffect.selfTargeting()
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, affectedPermanentId);
        if (target == null) {
            return;
        }

        target.setCantBlockThisCombat(true);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't block this combat."));
        log.info("Game {} - {} can't block this combat", gameData.id, target.getCard().getName());
    }
}
