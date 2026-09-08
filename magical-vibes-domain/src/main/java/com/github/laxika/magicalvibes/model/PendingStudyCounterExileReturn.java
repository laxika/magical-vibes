package com.github.laxika.magicalvibes.model;

/**
 * Marks a library-reveal choice as the controller choosing a study-counter card to return from
 * exile to their hand.
 */
public record PendingStudyCounterExileReturn() implements PendingInteraction {
}
