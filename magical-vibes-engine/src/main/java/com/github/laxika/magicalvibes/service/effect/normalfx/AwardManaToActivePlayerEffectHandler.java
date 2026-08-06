package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link AwardManaToActivePlayerEffect} — Eladamri's Vineyard's "that player adds
 * {@code {G}{G}}". The mana goes to the player recorded as the entry's target (the active player
 * at trigger time), falling back to the entry's controller when no target was recorded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardManaToActivePlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaToActivePlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardManaToActivePlayerEffect) effect;

        UUID recipientId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        if (pool == null) {
            return;
        }
        pool.add(e.color(), e.amount());

        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + e.amount() + " " + e.color().getCode() + "."));
        log.info("Game {} - {} adds {} {}", gameData.id, playerName, e.amount(), e.color());
    }
}
