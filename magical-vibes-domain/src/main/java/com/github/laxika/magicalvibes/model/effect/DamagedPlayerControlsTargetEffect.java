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

    /**
     * Returns the player who chooses the permanent target when the damage trigger is put on the
     * stack. Existing effects use the ability controller; effects worded "of that player's choice"
     * can return the damaged player instead.
     */
    default UUID targetChooserId(UUID damagedPlayerId, UUID sourceControllerId) {
        return sourceControllerId;
    }

    CardEffect forDamagedPlayer(UUID playerId);
}
