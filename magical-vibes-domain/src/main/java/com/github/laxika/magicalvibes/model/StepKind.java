package com.github.laxika.magicalvibes.model;

/**
 * The verb of a resolution step whose outcome a later step of the same resolution may read ("if you do", "that
 * many", "those creatures"). A step of each kind records a {@link StepOutcome} on the stack entry's
 * {@link ResolutionResults}, replacing the outcome of the previous step of that kind, even when nothing happened.
 *
 * <p>What each kind records ({@link StepOutcome#amount()} / {@link StepOutcome#objectIds()} /
 * {@link StepOutcome#playerIds()}):</p>
 * <ul>
 *   <li>{@link #DRAW}, {@link #DISCARD}, {@link #MILL}: the number of cards, those cards, the players they belonged to;</li>
 *   <li>{@link #EXILE}, {@link #DESTROY}, {@link #SACRIFICE}: the number of permanents, the permanents by their id as
 *       the step acted on them (a sacrifice made outside a departure, as by {@code MakeAnExampleEffect}: the card's
 *       id), their controllers as the step acted on them (CR 608.2h); a sacrifice step's {@code DepartureResult}s keep
 *       each one's last-known power, toughness and mana value; an {@link #EXILE} step that exiles cards from a
 *       graveyard, a library or a hand records those cards and their owners;</li>
 *   <li>{@link #RETURN}: the number of permanents or cards returned, the permanents (to a hand: by their id as the step
 *       acted on them; to the battlefield: the permanent each card became) or cards, their owners;</li>
 *   <li>{@link #CREATE_TOKEN}: the number of tokens, the tokens, the players who got them;</li>
 *   <li>{@link #DEAL_DAMAGE}: the total damage dealt, the permanents dealt damage, the players dealt damage;</li>
 *   <li>{@link #GAIN_LIFE}, {@link #LOSE_LIFE}: the total life gained or lost, nothing, the players;</li>
 *   <li>{@link #PUT_COUNTERS}, {@link #REMOVE_COUNTERS}: the number of counters, the permanents, their controllers;</li>
 *   <li>{@link #COUNTER_SPELL}, {@link #COPY_SPELL}: 1 when the spell was countered or copied, else 0.</li>
 * </ul>
 *
 * <p>A later step may read a step only if that step finishes before the next one starts (CR 608.2c): its handler never
 * prompts, pauses by resolving itself again before it changes anything, or pauses at the end of its step with its
 * result recorded by then. The recording handlers: draw, gain and lose life, exile a permanent, exile the source's or
 * the triggering card or target cards from a graveyard ({@code ExileGraveyardCardsEffect} except where its player
 * chooses at a prompt), exile the top card of a library, destroy, copy, create a token and mill (never prompt, or
 * rerun before any change); the self sacrifice ({@code departAsStep}), discard and
 * sacrifice (their prompts add to the result as they are answered, through {@code GameData.discarded} and
 * {@code DestructionSupport.sacrificeTogether}), the chosen return to hand
 * ({@code ReturnPermanentControlledByPlayerToHandEffect}: its {@code BounceCreature} choice adds the returned
 * permanent), put counters (a replacement order answered later adds the rest) and counter a spell; deal damage, return a card from a graveyard, remove counters and return to hand only on the paths that
 * don't prompt mid-step. A handler that pauses mid-step by rerunning itself after changes, or by a continuation of its
 * own, is never an earlier step. A damage, discard or sacrifice step records {@link StepOutcome#NONE} as it begins and the
 * engine adds to it as the damage is dealt, the cards discarded and the permanents sacrificed.</p>
 */
public enum StepKind {
    DRAW,
    DISCARD,
    MILL,
    EXILE,
    DESTROY,
    SACRIFICE,
    RETURN,
    CREATE_TOKEN,
    DEAL_DAMAGE,
    GAIN_LIFE,
    LOSE_LIFE,
    PUT_COUNTERS,
    REMOVE_COUNTERS,
    COUNTER_SPELL,
    COPY_SPELL
}
