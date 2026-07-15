package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesAllUnspentManaEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerLosesAllUnspentManaEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerLosesAllUnspentManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        ManaPool manaPool = gameData.playerManaPools.get(targetPlayerId);
        if (manaPool == null) {
            return;
        }

        manaPool.clear();
        manaPool.clearPersistentMana();

        String logMsg = entry.getCard().getName() + " empties the targeted player's mana pool.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} empties target player's mana pool", gameData.id, entry.getCard().getName());
    }
}
