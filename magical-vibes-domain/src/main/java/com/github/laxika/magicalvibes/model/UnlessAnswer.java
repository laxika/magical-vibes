package com.github.laxika.magicalvibes.model;

/**
 * Where the payer of an "unless" effect ({@code UnlessPaysEffect}, CR 118.12a) stands, kept on the resolving stack entry
 * so that another prompt answered before the resolution resumes (a trigger's or replacement's question during a
 * payment's choice) can't take it: the payer's answer, once given, and which of an any-of ransom's alternatives they
 * are asked (every earlier one declined or unpayable).
 *
 * @param effectIndex the index of the effect in the entry's effects to resolve
 * @param paid        {@code true} when the payer chose to pay (CR 118.12: the choice, not what happened),
 *                    {@code false} when they didn't, {@code null} while still asked
 * @param alternative the any-of ransom's alternative asked, by its place in Oracle order
 */
public record UnlessAnswer(int effectIndex, Boolean paid, int alternative) {
}
