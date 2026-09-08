package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a global turn-scoped creature attack restriction. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreaturesCantAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreaturesCantAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.creaturesCantAttackThisTurn = true;
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                permanent.setCantAttackThisTurn(true);
            }
        });
        gameLogService.append(gameData, GameLog.text("Creatures can't attack this turn."));
        log.info("Game {} - creatures can't attack this turn", gameData.id);
    }
}
