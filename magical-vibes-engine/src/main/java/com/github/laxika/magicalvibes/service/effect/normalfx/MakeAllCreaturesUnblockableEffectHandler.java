package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MakeAllCreaturesUnblockableEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeAllCreaturesUnblockableEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean controllerOnly = ((MakeAllCreaturesUnblockableEffect) effect).controllerOnly();
        var filter = ((MakeAllCreaturesUnblockableEffect) effect).filter();
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                effect, null, null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        String logEntry = filter != null
                ? "Matching creatures can't be blocked this turn."
                : controllerOnly
                ? "Creatures its controller controls can't be blocked this turn."
                : "Creatures can't be blocked this turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {}", gameData.id, logEntry);
    }
}
