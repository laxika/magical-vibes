package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesAllUnspentManaEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Empties each named player's mana pool, the mana that wouldn't empty from it included, and logs each player who had
 * mana to lose.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerLosesAllUnspentManaEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerLosesAllUnspentManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var lose = (TargetPlayerLosesAllUnspentManaEffect) effect;
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, lose.player())) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool == null) {
                continue;
            }
            boolean hadMana = manaPool.getTotalAllMana() > 0;
            manaPool.clear();
            manaPool.clearPersistentMana();
            if (hadMana) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(playerName + " loses all unspent mana ("
                        + FaceDownVisibility.sourceName(gameData, entry) + ")."));
                log.info("Game {} - {} loses all unspent mana", gameData.id, playerName);
            }
        }
    }
}
