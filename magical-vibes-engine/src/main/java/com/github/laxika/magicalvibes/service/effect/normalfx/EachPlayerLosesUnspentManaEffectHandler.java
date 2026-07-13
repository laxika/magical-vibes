package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesUnspentManaEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves "each player loses all unspent mana" (Worldpurge) by emptying every player's mana pool.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerLosesUnspentManaEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesUnspentManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool == null || manaPool.getTotalAllMana() == 0) {
                continue;
            }
            manaPool.clear();
            manaPool.clearPersistentMana();
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(playerId) + " loses all unspent mana (" + entry.getCard().getName() + ").");
            log.info("Game {} - {} loses all unspent mana ({})",
                    gameData.id, gameData.playerIdToName.get(playerId), entry.getCard().getName());
        }
    }
}
