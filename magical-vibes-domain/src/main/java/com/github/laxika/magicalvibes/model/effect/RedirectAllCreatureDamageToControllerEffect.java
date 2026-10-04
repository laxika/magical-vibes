package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntryType;
import java.util.List;
import java.util.UUID;

/** Registers an optional creature-damage redirect, or carries one pending replacement choice. */
public record RedirectAllCreatureDamageToControllerEffect(List<UUID> remainingControllers,
        boolean combatDamage, int targetIndex, boolean targetIsAttacker, boolean sourceHasDeathtouch,
        StackEntryType damageEntryType) implements CardEffect {
    public RedirectAllCreatureDamageToControllerEffect {
        remainingControllers = List.copyOf(remainingControllers);
    }
    public RedirectAllCreatureDamageToControllerEffect() {
        this(List.of(), false, -1, false, false, StackEntryType.ACTIVATED_ABILITY);
    }
}
