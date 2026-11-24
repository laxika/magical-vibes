package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[This creature] perpetually has base power and toughness N/M": the card of each permanent {@code subject} names
 * gets base power {@code power} and base toughness {@code toughness}, for as long as it is that card (digital only).
 * "Its base power and toughness perpetually become 2/2" after a conjure onto the battlefield is the step over
 * {@link Subjects#createdThisResolution()}.
 *
 * @param power     the new base power
 * @param toughness the new base toughness
 * @param subject   whose card; never a target
 */
public record PerpetuallySetSourceBasePowerToughnessEffect(int power, int toughness, PermanentSubject subject)
        implements CardEffect {

    public PerpetuallySetSourceBasePowerToughnessEffect {
        Objects.requireNonNull(subject, "subject");
        if (subject.declaresTarget()) {
            throw new IllegalArgumentException("The permanent is named, not targeted: " + subject);
        }
    }

    /** "This creature perpetually has base power and toughness {@code power}/{@code toughness}." */
    public PerpetuallySetSourceBasePowerToughnessEffect(int power, int toughness) {
        this(power, toughness, Subjects.source());
    }
}
