package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/** Resolves a special mana-producing effect that needs an inline interaction. */
public interface ManaAbilityEffectHandler {

    Class<? extends CardEffect> handledEffect();

    void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                 int manaMultiplier, boolean creatureSource);

    default boolean isRevertable() {
        return false;
    }
}
