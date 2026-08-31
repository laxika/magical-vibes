package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Static permission to spend mana as though it were mana of any color for activated abilities.
 */
public record SpendManaAsAnyColorForActivatedAbilitiesEffect(GrantScope scope)
        implements ActivatedAbilityManaColorPermissionEffect {

    public SpendManaAsAnyColorForActivatedAbilitiesEffect() {
        this(GrantScope.SELF);
    }

    public static SpendManaAsAnyColorForActivatedAbilitiesEffect controlledCreatures() {
        return new SpendManaAsAnyColorForActivatedAbilitiesEffect(GrantScope.ALL_OWN_CREATURES);
    }

    @Override
    public Set<ManaColor> manaColors() {
        return Set.copyOf(ManaColor.COLORS);
    }
}
