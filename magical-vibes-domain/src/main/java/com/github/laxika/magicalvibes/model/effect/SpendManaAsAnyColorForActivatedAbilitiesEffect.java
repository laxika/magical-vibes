package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Objects;

/**
 * Static permission to spend mana as though it were mana of any color for activated abilities (CR 609.4b).
 *
 * @param subject              whose activated abilities: {@code source()} ("this artifact's abilities") or a static
 *                             set ("creatures you control", "permanents you control but don't own")
 * @param borrowedAbilitiesOnly only the source's abilities it has from its {@link GainActivatedAbilitiesOfOwnCreaturesEffect}
 *                             ("you may spend mana as though it were mana of any color to activate those abilities",
 *                             Drana and Linvala, Scheming Fence)
 * @param fromColor            the color of mana it lets be spent that way ({@link ManaColor#BLUE} for the source's own
 *                             abilities, Quicksilver Elemental), or {@code null} for all mana
 */
public record SpendManaAsAnyColorForActivatedAbilitiesEffect(PermanentSubject subject, boolean borrowedAbilitiesOnly,
                                                            ManaColor fromColor) implements CardEffect {

    public SpendManaAsAnyColorForActivatedAbilitiesEffect {
        Objects.requireNonNull(subject, "subject");
        if (fromColor != null && (fromColor != ManaColor.BLUE || !(subject instanceof PermanentSubject.Source)
                || borrowedAbilitiesOnly)) {
            throw new IllegalArgumentException("Only blue mana for all of the source's own abilities can be singled out,"
                    + " not " + fromColor + " for " + subject);
        }
    }

    public SpendManaAsAnyColorForActivatedAbilitiesEffect(PermanentSubject subject) {
        this(subject, false, null);
    }

    public SpendManaAsAnyColorForActivatedAbilitiesEffect() {
        this(Subjects.source());
    }

    public static SpendManaAsAnyColorForActivatedAbilitiesEffect controlledCreatures() {
        return new SpendManaAsAnyColorForActivatedAbilitiesEffect(Subjects.creaturesYouControl());
    }

    /** "To activate those abilities": the abilities the source borrows. */
    public static SpendManaAsAnyColorForActivatedAbilitiesEffect forBorrowedAbilities() {
        return new SpendManaAsAnyColorForActivatedAbilitiesEffect(Subjects.source(), true, null);
    }

    /** "You may spend blue mana as though it were mana of any color to pay the activation costs of this creature's abilities". */
    public static SpendManaAsAnyColorForActivatedAbilitiesEffect blueManaForOwnAbilities() {
        return new SpendManaAsAnyColorForActivatedAbilitiesEffect(Subjects.source(), false, ManaColor.BLUE);
    }
}
