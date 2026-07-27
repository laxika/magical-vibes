package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

/**
 * Factories for the mana abilities lands and mana rocks repeat verbatim. The reminder text
 * is derived from the colour, so a dual land's several abilities cannot drift out of sync
 * with what they actually produce.
 *
 * <p>Only unconditional "{@code {T}: Add …}" abilities belong here. Anything with a cost
 * beyond tapping, a restriction on what the mana may pay for, or a drawback (pain lands,
 * lands that enter tapped) is card-specific and should be built directly.
 */
public final class ManaAbilities {

    private ManaAbilities() {
    }

    /** {@code {T}: Add} one mana of {@code color}. */
    public static ActivatedAbility tapFor(ManaColor color) {
        return new ActivatedAbility(true, null,
                List.of(new AwardManaEffect(color)),
                "{T}: Add {" + color.getCode() + "}.");
    }

    /** {@code {T}: Add one mana of any color.} */
    public static ActivatedAbility tapForAnyColor() {
        return new ActivatedAbility(true, null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color.");
    }
}
