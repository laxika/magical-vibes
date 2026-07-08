package com.github.laxika.magicalvibes.model.effect;

/**
 * When a {@link FlickerEffect} returns the exiled permanent(s) to the battlefield.
 *
 * <ul>
 *   <li>{@code IMMEDIATE} — return right away as part of the same resolution (Cloudshift-style).</li>
 *   <li>{@code AT_STEP} — schedule a delayed return at the beginning of the next {@code returnStep}
 *       (Glimmerpoint Stag-style), surviving the source leaving the battlefield.</li>
 * </ul>
 */
public enum ReturnTiming {
    IMMEDIATE,
    AT_STEP
}
