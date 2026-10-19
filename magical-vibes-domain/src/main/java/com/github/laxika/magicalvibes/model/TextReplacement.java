package com.github.laxika.magicalvibes.model;

/**
 * One word substitution applied by a text-changing effect (CR 612).
 *
 * <p>Text-changing effects apply in timestamp order (CR 613.7), so a later effect can rewrite a word
 * an earlier one introduced. The {@code timestamp} only orders application against other text
 * changes (Swirl the Mists); it is not part of the substitution's identity, so {@link #equals} and
 * {@link #hashCode} ignore it.
 *
 * @param untilEndOfTurn when true the substitution wears off at the cleanup step (Whim of Volrath);
 *                       when false it lasts as long as the permanent stays on the battlefield
 *                       (Mind Bend, Magical Hack).
 * @param timestamp      the CR 613.7 timestamp of the effect that made the change, or 0 when unknown
 *                       (applied before any timestamped change)
 */
public record TextReplacement(String fromWord, String toWord, boolean untilEndOfTurn, long timestamp) {

    public TextReplacement(String fromWord, String toWord, boolean untilEndOfTurn) {
        this(fromWord, toWord, untilEndOfTurn, 0L);
    }

    public TextReplacement(String fromWord, String toWord) {
        this(fromWord, toWord, false, 0L);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TextReplacement that
                && fromWord.equals(that.fromWord)
                && toWord.equals(that.toWord)
                && untilEndOfTurn == that.untilEndOfTurn;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(fromWord, toWord, untilEndOfTurn);
    }
}
