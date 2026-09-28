package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Sacrifices the permanent that caused the trigger, then conjures a fresh copy of it. */
public record SacrificeTriggeringPermanentThenConjureDuplicateEffect(
        CreateTokenCopyOfTargetPermanentEffect copyProfile
) implements CardEffect {

    public SacrificeTriggeringPermanentThenConjureDuplicateEffect {
        Objects.requireNonNull(copyProfile, "copyProfile");
    }
}
