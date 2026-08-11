package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.EnumMap;
import java.util.Map;

/** True when the given color is tied for the most common color among all permanents. */
public record ColorMostCommonAmongAllPermanents(CardColor color) implements Condition {

    @Override
    public String conditionName() {
        return color.name().toLowerCase() + " is most common among all permanents or tied";
    }

    @Override
    public String conditionNotMetReason() {
        return color.name().toLowerCase() + " is not most common among all permanents and is not tied";
    }

    public static boolean isMostCommon(GameData gameData, CardColor color) {
        Map<CardColor, Integer> counts = new EnumMap<>(CardColor.class);
        for (CardColor candidate : CardColor.values()) {
            counts.put(candidate, 0);
        }
        for (var playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, java.util.List.of())) {
                for (CardColor permanentColor : permanent.getEffectiveColors()) {
                    counts.merge(permanentColor, 1, Integer::sum);
                }
            }
        }
        int colorCount = counts.get(color);
        return counts.values().stream().allMatch(count -> colorCount >= count);
    }
}
