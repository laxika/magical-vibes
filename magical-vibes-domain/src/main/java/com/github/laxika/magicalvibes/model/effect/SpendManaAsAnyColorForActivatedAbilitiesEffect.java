package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Static self-only permission: mana may be spent as though it were mana of any color to pay the
 * source creature's activated ability costs.
 */
public record SpendManaAsAnyColorForActivatedAbilitiesEffect() implements ActivatedAbilityManaColorPermissionEffect {

    @Override
    public Set<ManaColor> manaColors() {
        return Set.copyOf(ManaColor.COLORS);
    }
}
