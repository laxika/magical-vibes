package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        this(List.of(new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(), additionalEffect)));
    }

    public BecomeCopyOfExiledCreaturePermanentlyEffect() {
        this(List.of());
    }
}
