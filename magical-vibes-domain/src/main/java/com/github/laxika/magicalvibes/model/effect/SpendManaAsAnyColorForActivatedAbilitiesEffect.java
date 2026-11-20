package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Static permission to spend mana as though it were mana of any color for activated abilities.
 */
public record SpendManaAsAnyColorForActivatedAbilitiesEffect(PermanentSubject subject)
        implements ActivatedAbilityManaColorPermissionEffect {

    public SpendManaAsAnyColorForActivatedAbilitiesEffect() {
        this(Subjects.source());
    }

    public static SpendManaAsAnyColorForActivatedAbilitiesEffect controlledCreatures() {
        return new SpendManaAsAnyColorForActivatedAbilitiesEffect(Subjects.creaturesYouControl());
    }

    @Override
    public Set<ManaColor> manaColors() {
        return Set.copyOf(ManaColor.COLORS);
    }
}
