package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantCastNoncreatureSpellsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetPlayerCantCastNoncreatureSpellsUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerCantCastNoncreatureSpellsUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) return;

        gameData.playersCantCastNoncreatureSpellsUntilControllerNextTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(targetPlayerId);

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(targetPlayerId)
                        + " can't cast noncreature spells until your next turn."));
    }
}
