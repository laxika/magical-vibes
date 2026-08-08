package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerCantCastSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetSpellControllerCantCastSpellsThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetSpellControllerCantCastSpellsThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                UUID restrictedId = se.getControllerId();
                gameData.playersSilencedThisTurn.add(restrictedId);
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(restrictedId) + " can't cast spells this turn."));
                return;
            }
        }
        log.info("Game {} - Target spell no longer on stack for cast restriction", gameData.id);
    }
}
