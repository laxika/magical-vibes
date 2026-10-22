package com.github.laxika.magicalvibes.model.effect;

/**
 * When a {@link FlickerEffect} returns the exiled permanent(s) to the battlefield.
 *
 * <ul>
 *   <li>{@code IMMEDIATE} — return right away as part of the same resolution (Cloudshift-style).</li>
 *   <li>{@code AT_STEP} — schedule a delayed return at the beginning of the next {@code returnStep}
 *       (Glimmerpoint Stag-style), surviving the source leaving the battlefield.</li>
 *   <li>{@code NONE} — exile only; a later effect of the same resolution decides whether and when the
 *       card returns (Gilraen, Dúnedain Protector's "exile it. You may return it...").</li>
 * </ul>
 */
public enum ReturnTiming {
    IMMEDIATE,
    AT_STEP,
    NONE
}
