package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

import java.util.List;

/**
 * Makes the source permanent a permanent copy of the creature exiled as this ability's cost.
 * Additional abilities model copy exceptions that grant the source extra abilities.
 */
public record BecomeCopyOfExiledCreaturePermanentlyEffect(
        List<Ability> additionalAbilities
) implements CardEffect {

    public BecomeCopyOfExiledCreaturePermanentlyEffect {
        additionalAbilities = additionalAbilities == null ? List.of() : List.copyOf(additionalAbilities);
    }

    public BecomeCopyOfExiledCreaturePermanentlyEffect(CardEffect additionalEffect) {
        this(List.of(LegacySlots.triggered(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, List.of(additionalEffect))));
    }

    public BecomeCopyOfExiledCreaturePermanentlyEffect() {
        this(List.of());
    }
}
