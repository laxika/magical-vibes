package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntryType;
import java.util.List;
import java.util.UUID;

/** Optional prevention by an active Alchemist; the payload retains an impending damage event. */
public record PreventDamageToControllerPerClericEffect(UUID damagedPlayerId, UUID alchemistId,
        List<UUID> remainingAlchemistIds, boolean combatDamage, StackEntryType damageEntryType,
        boolean unpreventableDamage) implements CardEffect {
    public PreventDamageToControllerPerClericEffect {
        remainingAlchemistIds = List.copyOf(remainingAlchemistIds);
    }
    public PreventDamageToControllerPerClericEffect() {
        this(null, null, List.of(), false, StackEntryType.ACTIVATED_ABILITY, false);
    }
}
