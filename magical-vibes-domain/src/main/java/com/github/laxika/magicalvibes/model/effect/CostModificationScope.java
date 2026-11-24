package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Determines whose spells are affected by a cost modification effect.
 */
public enum CostModificationScope {

    /**
     * Affects spells cast by the controller of the permanent carrying this effect.
     */
    SELF,

    /**
     * Affects spells cast by opponents of the permanent's controller.
     */
    OPPONENT,

    /**
     * Affects spells cast by every player, regardless of who controls the permanent
     * (e.g. Arcane Melee: "Instant and sorcery spells cost {2} less to cast.").
     */
    ALL;

    /** Whether a payment by {@code playerId} is in this scope, the modifier's source being controlled by {@code controllerId}. */
    public boolean names(UUID controllerId, UUID playerId) {
        boolean own = playerId != null && playerId.equals(controllerId);
        return switch (this) {
            case SELF -> own;
            case OPPONENT -> playerId != null && !own;
            case ALL -> true;
        };
    }
}
