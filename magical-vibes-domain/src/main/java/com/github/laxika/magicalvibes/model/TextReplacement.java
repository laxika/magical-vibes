package com.github.laxika.magicalvibes.model;

/**
 * One word substitution applied by a text-changing effect (CR 612).
 *
 * @param untilEndOfTurn when true the substitution wears off at the cleanup step (Whim of Volrath);
 *                       when false it lasts as long as the permanent stays on the battlefield
 *                       (Mind Bend, Magical Hack).
 */
public record TextReplacement(String fromWord, String toWord, boolean untilEndOfTurn) {

    public TextReplacement(String fromWord, String toWord) {
        this(fromWord, toWord, false);
    }
}
