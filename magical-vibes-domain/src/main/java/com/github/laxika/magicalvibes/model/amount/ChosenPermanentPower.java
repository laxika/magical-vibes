package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective power (never negative) of the permanent chosen during this activation — e.g. the
 * creature tapped to pay a {@code TapCreatureCost(trackTappedCreaturePower = true)}. Evaluated as
 * the ability resolves from the live permanent, matching the ruling that the value is checked at
 * resolution rather than when the creature was tapped (Impelled Giant). Evaluates to 0 when the
 * chosen permanent has left the battlefield or none was recorded (mirrors {@link SourcePower}).
 */
public record ChosenPermanentPower() implements DynamicAmount {
}
