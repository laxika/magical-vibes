package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * Grants protection from every mana value other than the number chosen for the source permanent.
 * No protection is granted until the chosen number is one of {@code possibleChosenNumbers}.
 *
 * @param possibleChosenNumbers the values the source permanent can choose
 */
public record ProtectionFromAllOtherManaValuesEffect(Set<Integer> possibleChosenNumbers)
        implements ProtectionGrantingEffect {

    public ProtectionFromAllOtherManaValuesEffect {
        possibleChosenNumbers = Set.copyOf(possibleChosenNumbers);
    }

    @Override
    public Set<Integer> protectionFromManaValuesOtherThanChosen() {
        return possibleChosenNumbers;
    }
}
