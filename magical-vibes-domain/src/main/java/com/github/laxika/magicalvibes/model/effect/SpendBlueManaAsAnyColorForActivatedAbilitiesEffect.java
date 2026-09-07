package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Static self-only permission: blue mana may be spent as though it were mana of any color to pay
 * activation costs of the source creature's abilities.
 */
public record SpendBlueManaAsAnyColorForActivatedAbilitiesEffect() implements ActivatedAbilityManaColorPermissionEffect {

    @Override
    public Set<ManaColor> manaColors() {
        return Set.of(ManaColor.BLUE);
    }
}
