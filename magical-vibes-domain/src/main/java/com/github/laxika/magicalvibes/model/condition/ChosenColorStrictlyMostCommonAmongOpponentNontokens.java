package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The source permanent's chosen color is strictly the most common color among nontoken permanents
 * controlled by an opponent (two-player: the single opponent). Multicolored permanents count for
 * each of their colors; colorless permanents are ignored. Used by Call to Arms.
 */
public record ChosenColorStrictlyMostCommonAmongOpponentNontokens() implements Condition {

    @Override
    public String conditionName() {
        return "chosen color is strictly most common among opponent nontoken permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "chosen color is not strictly most common among opponent nontoken permanents";
    }

    /**
     * Shared by {@link com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService}
     * and Call to Arms's state-triggered sacrifice (inverted). Returns {@code false} when no color
     * has been chosen yet.
     */
    public static boolean isStrictlyMostCommon(GameData gameData, Permanent source, UUID controllerId) {
        return isStrictlyMostCommon(gameData, source, controllerId, Permanent::getEffectiveColors);
    }

    public static boolean isStrictlyMostCommon(GameData gameData, Permanent source, UUID controllerId,
            java.util.function.Function<Permanent, ? extends java.util.Collection<CardColor>> colors) {
        CardColor chosen = source.getChosenColor();
        if (chosen == null || controllerId == null) {
            return false;
        }
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (source.getChosenPlayerIds().isEmpty() ? opponentId.equals(controllerId)
                    : !source.getChosenPlayerIds().contains(opponentId)) {
                continue;
            }
            if (isStrictlyMostCommonAmong(gameData.playerBattlefields.get(opponentId), chosen, colors)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStrictlyMostCommonAmong(List<Permanent> battlefield, CardColor chosen,
            java.util.function.Function<Permanent, ? extends java.util.Collection<CardColor>> colors) {
        if (battlefield == null || battlefield.isEmpty()) {
            return false;
        }
        Map<CardColor, Integer> counts = new EnumMap<>(CardColor.class);
        for (CardColor color : CardColor.values()) {
            counts.put(color, 0);
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().isToken()) {
                continue;
            }
            for (CardColor color : colors.apply(permanent)) {
                counts.merge(color, 1, Integer::sum);
            }
        }
        int chosenCount = counts.get(chosen);
        if (chosenCount == 0) {
            return false;
        }
        for (CardColor color : CardColor.values()) {
            if (color == chosen) {
                continue;
            }
            if (counts.get(color) >= chosenCount) {
                return false;
            }
        }
        return true;
    }
}
