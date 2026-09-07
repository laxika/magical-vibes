package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/** Resolves a special mana-producing effect that needs an inline interaction. */
public interface ManaAbilityEffectHandler {

    Class<? extends CardEffect> handledEffect();

    void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                 CardEffect effect, int manaMultiplier, boolean creatureSource);

    /** Returns the unmultiplied amount produced by this effect for replacement-effect accounting. */
    default int calculateManaProduction(GameData gameData, UUID playerId, Permanent permanent,
                                         CardEffect effect, int xValue) {
        return 0;
    }

    /** Returns the colors currently available from a condition-dependent producer, if known. */
    default List<ManaColor> availableManaColors(GameData gameData, UUID playerId,
                                                Permanent permanent, CardEffect effect) {
        return List.of();
    }

    default boolean isRevertable() {
        return false;
    }
}
