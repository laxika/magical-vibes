package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/**
 * Static permission to spend mana as though it were mana of any color for activated abilities.
 *
 * @param subject              whose activated abilities: {@code source()} ("this artifact's abilities") or a static
 *                             set ("creatures you control")
 * @param borrowedAbilitiesOnly only the source's abilities it has from its {@link GainActivatedAbilitiesOfOwnCreaturesEffect}
 *                             ("you may spend mana as though it were mana of any color to activate those abilities",
 *                             Drana and Linvala, Scheming Fence)
 */
public record SpendManaAsAnyColorForActivatedAbilitiesEffect(PermanentSubject subject, boolean borrowedAbilitiesOnly)
        implements ActivatedAbilityManaColorPermissionEffect {

    public SpendManaAsAnyColorForActivatedAbilitiesEffect(PermanentSubject subject) {
        this(subject, false);
    }

    public SpendManaAsAnyColorForActivatedAbilitiesEffect() {
        this(Subjects.source());
    }

    public static SpendManaAsAnyColorForActivatedAbilitiesEffect controlledCreatures() {
        return new SpendManaAsAnyColorForActivatedAbilitiesEffect(Subjects.creaturesYouControl());
    }

    /** "To activate those abilities": the abilities the source borrows. */
    public static SpendManaAsAnyColorForActivatedAbilitiesEffect forBorrowedAbilities() {
        return new SpendManaAsAnyColorForActivatedAbilitiesEffect(Subjects.source(), true);
    }

    @Override
    public Set<ManaColor> manaColors() {
        return Set.copyOf(ManaColor.COLORS);
    }
}
