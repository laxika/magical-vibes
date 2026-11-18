package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * Grants protection from each mana value other than a number (CR 702.16i). No protection is granted until that
 * number is one of {@code possibleChosenNumbers}.
 *
 * @param possibleChosenNumbers the values the number can be
 * @param chosenNumber          the number, when the effect fixes it (a shared copy of the ability); {@code null} for
 *                              the number chosen for the permanent that has the ability
 */
public record ProtectionFromAllOtherManaValuesEffect(Set<Integer> possibleChosenNumbers, Integer chosenNumber)
        implements ProtectionGrantingEffect {

    public ProtectionFromAllOtherManaValuesEffect {
        possibleChosenNumbers = Set.copyOf(possibleChosenNumbers);
    }

    /** Protection from each mana value other than the number chosen for the permanent that has the ability. */
    public ProtectionFromAllOtherManaValuesEffect(Set<Integer> possibleChosenNumbers) {
        this(possibleChosenNumbers, null);
    }

    @Override
    public Set<Integer> protectionFromManaValuesOtherThanChosen() {
        return possibleChosenNumbers;
    }

    @Override
    public Integer protectionFromManaValuesOtherThanFixed() {
        return chosenNumber;
    }
}
