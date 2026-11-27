package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player shuffles all permanents they own into their library, then reveals that many cards from the top of their
 * library" (Morphic Tide; Glimpse of Tomorrow's "you"): each player {@code players} names, in APNAP order, counts the
 * permanents they own (tokens and a commander that goes elsewhere included), shuffles them into their library, then
 * reveals that many cards from its top. The revealed cards stay on top (CR 701.20b); the step records them
 * ({@code StepKind.REVEAL}), so "put all … cards revealed this way onto the battlefield" is
 * {@code PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(filter))} and "the rest on the bottom" is
 * {@code PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay())}.
 *
 * @param players whose owned permanents are shuffled in and who reveal
 */
public record MorphicTideEffect(PlayerSubject players) implements CardEffect {
}
