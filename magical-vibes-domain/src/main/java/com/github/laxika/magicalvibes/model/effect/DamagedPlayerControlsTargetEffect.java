package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * A damage trigger whose permanent target is chosen from the player dealt damage.
 * The damaged player's identity is bound before target selection.
 */
public interface DamagedPlayerControlsTargetEffect extends CardEffect {

    PermanentPredicate predicate();

    default int minimumDamage() {
        return 0;
    }

    CardEffect forDamagedPlayer(UUID playerId);
}
