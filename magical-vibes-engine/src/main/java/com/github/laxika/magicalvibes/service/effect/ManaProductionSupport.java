package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceTargetLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

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

    /** Applies both turn-scoped replacements and replacements attached to a specific source land. */
    public static ManaColor effectiveColor(GameData gameData, UUID sourceControllerId,
                                           Permanent source, ManaColor color) {
        ManaColor effectiveColor = effectiveColor(gameData, sourceControllerId, color);
        if (source == null || effectiveColor == null) {
            return effectiveColor;
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floatingEffect : gameData.floatingEffects) {
                if (!source.getId().equals(floatingEffect.affectedPermanentId())
                        || !(floatingEffect.effect() instanceof ReplaceTargetLandManaWithColorEffect replacement)
                        || effectiveColor != replacement.fromColor()) {
                    continue;
                }
                return replacement.replacementColor();
            }
        }
        return effectiveColor;
    }

    public static void add(GameData gameData, UUID sourceControllerId, ManaPool pool,
                           ManaColor color, int amount) {
        pool.add(effectiveColor(gameData, sourceControllerId, color), amount);
    }

    public static void add(GameData gameData, UUID sourceControllerId, Permanent source,
                           ManaPool pool, ManaColor color, int amount) {
        pool.add(effectiveColor(gameData, sourceControllerId, source, color), amount);
    }
}
