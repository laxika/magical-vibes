package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceColoredManaWithWhiteUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import java.util.List;
import java.util.UUID;

/** Shared application of turn-scoped mana-production replacement effects. */
public final class ManaProductionSupport {

    private ManaProductionSupport() {
    }

    public static ManaColor effectiveColor(GameData gameData, UUID sourceControllerId, ManaColor color) {
        if (color != null && color != ManaColor.COLORLESS && sourceControllerId != null
                && gameData.hasFloatingEffectOnPlayer(sourceControllerId,
                ReplaceColoredManaWithWhiteUntilEndOfTurnEffect.class)) {
            return ManaColor.WHITE;
        }
        return color;
    }

    /**
     * Applies both turn-scoped replacements and the "instead of [type]" land-mana replacements floated on the
     * specific source land (Quarum Trench Gnomes).
     */
    public static ManaColor effectiveColor(GameData gameData, UUID sourceControllerId,
                                           Permanent source, ManaColor color) {
        ManaColor effectiveColor = effectiveColor(gameData, sourceControllerId, color);
        if (source == null || effectiveColor == null) {
            return effectiveColor;
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floatingEffect : gameData.floatingEffects) {
                if (!source.getId().equals(floatingEffect.affectedPermanentId())
                        || !(floatingEffect.effect() instanceof ReplaceLandManaWithColorEffect replacement)
                        || replacement.color() == null
                        || !replacement.replacedTypes().contains(effectiveColor)) {
                    continue;
                }
                return replacement.color();
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

    /** Returns the five colors present in the activating player's commander color identity. */
    public static List<ManaColor> commanderColorIdentity(GameData gameData, UUID playerId) {
        List<Card> commandZone = gameData.playerCommanders.get(playerId);
        if (commandZone == null || commandZone.isEmpty()) {
            return List.of();
        }
        return ManaColor.COLORS.stream()
                .filter(color -> commandZone.stream()
                        .anyMatch(card -> card.getColorIdentity().contains(CardColor.valueOf(color.name()))))
                .toList();
    }
}
