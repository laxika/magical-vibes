package com.github.laxika.magicalvibes.model.replacement;

/**
 * One replacement or prevention effect that currently applies to a {@link ReplacementEvent}.
 *
 * @param key   identifies the effect instance (its family and source) for CR 614.5 bookkeeping
 * @param label unique text shown to the choosing player
 * @param kind  how the effect modifies the event
 * @param pure  {@code true} when applying it only changes the event itself — no shield consumed,
 *              no rider, no prompt — so it can be previewed without touching the game
 */
public record ReplacementOption(String key, String label, Kind kind, boolean pure) {

    /** CR 616.1a puts self-replacement effects ahead of every other kind. */
    public enum Kind {
        SELF_REPLACEMENT,
        REPLACEMENT,
        REDIRECT,
        PREVENTION
    }
}
