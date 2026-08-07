package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeAllCreaturesUnblockableEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeAllCreaturesUnblockableEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean controllerOnly = ((MakeAllCreaturesUnblockableEffect) effect).controllerOnly();
        gameData.forEachPermanent((playerId, perm) -> {
            if (controllerOnly && !playerId.equals(entry.getControllerId())) {
                return;
            }
            if (gameQueryService.isCreature(gameData, perm)) {
                perm.setCantBeBlocked(true);
            }
        });

        String logEntry = controllerOnly
                ? "Creatures its controller controls can't be blocked this turn."
                : "Creatures can't be blocked this turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - All creatures can't be blocked this turn", gameData.id);
    }
}
