package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

import java.util.UUID;

/** Shared application of turn-scoped mana-production replacement effects. */
public final class ManaProductionSupport {

    private ManaProductionSupport() {
    }

    public static ManaColor effectiveColor(GameData gameData, UUID sourceControllerId, ManaColor color) {
        if (color != null && color != ManaColor.COLORLESS && sourceControllerId != null
                && gameData.playersWithColoredManaReplacementThisTurn.contains(sourceControllerId)) {
            return ManaColor.WHITE;
        }
        return color;
    }

    public static void add(GameData gameData, UUID sourceControllerId, ManaPool pool,
                           ManaColor color, int amount) {
        pool.add(effectiveColor(gameData, sourceControllerId, color), amount);
    }
}
