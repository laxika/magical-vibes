package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Which players' graveyards a graveyard target may be drawn from. Carried by
 * {@code TargetPredicate.GraveyardCards} — the single declaration of graveyard scope, so every
 * enumeration and validation path resolves it through {@link #graveyardOwners(List, UUID)} rather
 * than re-deriving it from the effect.
 */
public enum GraveyardSearchScope {

    CONTROLLERS_GRAVEYARD,
    ALL_GRAVEYARDS,
    OPPONENT_GRAVEYARD;

    /**
     * The players whose graveyards this scope searches, from {@code controllerId}'s point of view,
     * in {@code orderedPlayerIds} order.
     */
    public List<UUID> graveyardOwners(List<UUID> orderedPlayerIds, UUID controllerId) {
        return switch (this) {
            case CONTROLLERS_GRAVEYARD -> List.of(controllerId);
            case OPPONENT_GRAVEYARD -> orderedPlayerIds.stream()
                    .filter(id -> !id.equals(controllerId))
                    .toList();
            case ALL_GRAVEYARDS -> orderedPlayerIds;
        };
    }
}
