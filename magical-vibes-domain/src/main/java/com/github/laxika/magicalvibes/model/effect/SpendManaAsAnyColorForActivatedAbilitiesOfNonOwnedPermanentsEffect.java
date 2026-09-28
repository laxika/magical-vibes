package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Static permission to spend mana as though it were mana of any color to activate abilities of
 * permanents the controller controls but does not own.
 */
public record SpendManaAsAnyColorForActivatedAbilitiesOfNonOwnedPermanentsEffect()
        implements ActivatedAbilityManaColorPermissionEffect {

    @Override
    public Set<ManaColor> manaColors() {
        return Set.copyOf(ManaColor.COLORS);
    }

    @Override
    public boolean appliesToControlledNonOwnedPermanentsOnly() {
        return true;
    }
}
