package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaValueParity;

import java.util.OptionalInt;
import java.util.Set;

/**
 * Grants protection from the sources whose mana value has a quality (CR 702.16a; the "each" forms, CR 702.16i), checked by
 * {@code GameQueryService.hasProtectionFromSourceManaValue()}: {@link Quality#AT_LEAST} "protection from mana value N
 * or greater" (the {@code (int)} constructor; Mistmeadow Skulk), {@link Quality#AT_MOST} "mana value N or less"
 * ({@link #atMost(int)}; Reaver Titan), {@link Quality#PARITY} "each mana value of the chosen quality"
 * ({@link #chosenParity()}; Lavabrink Venturer), {@link Quality#OTHER_THAN} "each mana value other than the chosen
 * number" ({@link #otherThanChosenNumber(Set)}; Haktos the Unscarred). Until the quality or number is chosen for the
 * permanent that has the ability, the chosen forms grant no protection; a shared copy of the ability fixes what was
 * chosen ({@link #parity(ManaValueParity)}, {@link #otherThan(int)}).
 *
 * @param quality               which mana values it protects from
 * @param number                {@link Quality#AT_LEAST} / {@link Quality#AT_MOST}: the inclusive bound;
 *                              {@link Quality#OTHER_THAN}: the fixed number, or {@code null} for the chosen one
 * @param parity                {@link Quality#PARITY}: the fixed odd/even quality, or {@code null} for the chosen one
 * @param possibleChosenNumbers {@link Quality#OTHER_THAN}: the values the number can be; empty otherwise
 */
public record ProtectionFromManaValueEffect(Quality quality, Integer number, ManaValueParity parity,
                                            Set<Integer> possibleChosenNumbers) implements ProtectionGrantingEffect {

    /** The mana-value quality protected from. */
    public enum Quality {
        AT_LEAST, AT_MOST, PARITY, OTHER_THAN
    }

    public ProtectionFromManaValueEffect {
        possibleChosenNumbers = Set.copyOf(possibleChosenNumbers);
        if ((quality == Quality.AT_LEAST || quality == Quality.AT_MOST) && number == null) {
            throw new IllegalArgumentException("A mana-value bound needs its number");
        }
        if ((quality == Quality.OTHER_THAN) == possibleChosenNumbers.isEmpty()) {
            throw new IllegalArgumentException("Only the other-than quality has possible numbers");
        }
    }

    /** "Protection from mana value {@code minManaValue} or greater". */
    public ProtectionFromManaValueEffect(int minManaValue) {
        this(Quality.AT_LEAST, minManaValue, null, Set.of());
    }

    /** "Protection from mana value {@code maxManaValue} or less". */
    public static ProtectionFromManaValueEffect atMost(int maxManaValue) {
        return new ProtectionFromManaValueEffect(Quality.AT_MOST, maxManaValue, null, Set.of());
    }

    /** "Protection from each mana value of the chosen quality". */
    public static ProtectionFromManaValueEffect chosenParity() {
        return parity(null);
    }

    /** Protection from each mana value of {@code parity} ({@code null}: the chosen quality). */
    public static ProtectionFromManaValueEffect parity(ManaValueParity parity) {
        return new ProtectionFromManaValueEffect(Quality.PARITY, null, parity, Set.of());
    }

    /** "Protection from each mana value other than the chosen number", the number one of {@code possibleChosenNumbers}. */
    public static ProtectionFromManaValueEffect otherThanChosenNumber(Set<Integer> possibleChosenNumbers) {
        return new ProtectionFromManaValueEffect(Quality.OTHER_THAN, null, null, possibleChosenNumbers);
    }

    /** Protection from each mana value other than {@code number}. */
    public static ProtectionFromManaValueEffect otherThan(int number) {
        return new ProtectionFromManaValueEffect(Quality.OTHER_THAN, number, null, Set.of(number));
    }

    @Override
    public OptionalInt protectionFromManaValueAtLeast() {
        return quality == Quality.AT_LEAST ? OptionalInt.of(number) : OptionalInt.empty();
    }

    @Override
    public OptionalInt protectionFromManaValueAtMost() {
        return quality == Quality.AT_MOST ? OptionalInt.of(number) : OptionalInt.empty();
    }

    @Override
    public boolean protectionFromManaValueParity() {
        return quality == Quality.PARITY;
    }

    @Override
    public ManaValueParity protectionFromFixedManaValueParity() {
        return parity;
    }

    @Override
    public Set<Integer> protectionFromManaValuesOtherThanChosen() {
        return possibleChosenNumbers;
    }

    @Override
    public Integer protectionFromManaValuesOtherThanFixed() {
        return quality == Quality.OTHER_THAN ? number : null;
    }
}
