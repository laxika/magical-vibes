package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.List;
import java.util.Map;

/**
 * Makes the source permanent a permanent copy of the creature exiled as this ability's cost.
 * Additional effect registrations model copy exceptions that grant the source extra abilities.
 */
public record BecomeCopyOfExiledCreaturePermanentlyEffect(
        Map<EffectSlot, List<CardEffect>> additionalSlotEffects
) implements CardEffect {

    public BecomeCopyOfExiledCreaturePermanentlyEffect {
        additionalSlotEffects = additionalSlotEffects == null ? Map.of() : Map.copyOf(additionalSlotEffects);
    }

    public BecomeCopyOfExiledCreaturePermanentlyEffect(CardEffect additionalEffect) {
        this(Map.of(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, List.of(additionalEffect)));
    }

    public BecomeCopyOfExiledCreaturePermanentlyEffect() {
        this(Map.of());
    }
}
