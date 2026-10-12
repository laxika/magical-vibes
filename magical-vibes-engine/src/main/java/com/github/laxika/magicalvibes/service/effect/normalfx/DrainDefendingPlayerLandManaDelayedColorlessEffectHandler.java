package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrainDefendingPlayerLandManaDelayedColorlessEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link DrainDefendingPlayerLandManaDelayedColorlessEffect} (Pygmy Hippo): defending
 * player activates a mana ability of each land, loses all unspent mana, and the controller queues
 * that many {@code {C}} for their next main phase this turn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrainDefendingPlayerLandManaDelayedColorlessEffectHandler implements NormalEffectHandlerBean {

    private final DrainTargetPlayersLandManaEffectHandler drainTargetPlayersLandManaEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrainDefendingPlayerLandManaDelayedColorlessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID defendingPlayerId = entry.getTargetId();
        if (defendingPlayerId == null || !gameData.playerIds.contains(defendingPlayerId)) {
            return;
        }
        ManaPool defendingPool = gameData.playerManaPools.get(defendingPlayerId);
        if (defendingPool == null) {
            return;
        }

        drainTargetPlayersLandManaEffectHandler.drainForDelayedColorless(gameData, entry);
    }
}
