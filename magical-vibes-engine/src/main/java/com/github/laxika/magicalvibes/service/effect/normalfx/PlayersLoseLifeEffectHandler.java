package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Makes the players of a {@link PlayersLoseLifeEffect} step lose its amounts through {@link LifeSupport#applyLifeLosses}.
 * A player who has left the game is skipped.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the life losses are its only change, so every Lich's Duel Mastery
 * sacrifice that replaces one of them is asked about (CR 616.1) before any life is lost.
 */
@Component
@RequiredArgsConstructor
public class PlayersLoseLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayersLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        ((PlayersLoseLifeEffect) effect).losses().forEach((playerId, amount) -> {
            if (gameData.playerIds.contains(playerId)) {
                losses.put(playerId, amount);
            }
        });
        lifeSupport.applyLifeLosses(gameData, losses, entry.getCard().getName());
    }
}
