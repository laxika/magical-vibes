package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

/**
 * The player with strictly the most life among all players gains control of the source creature
 * (Ghazbán Ogre). If two or more players are tied for the most life, no one gains control.
 */
public record PlayerWithMostLifeGainsControlOfSourceCreatureEffect() implements CardEffect {

    public static boolean hasUniqueLifeLeader(GameData gameData) {
        int highestLife = gameData.orderedPlayerIds.stream()
                .mapToInt(gameData::getLife)
                .max()
                .orElse(0);
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> gameData.getLife(playerId) == highestLife)
                .limit(2)
                .count() == 1;
    }
}
