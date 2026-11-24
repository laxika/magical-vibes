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
 *       id), their controllers as the step acted on them (CR 608.2h); a sacrifice step's and a batch destroy's
 *       ({@code DestroyAllPermanentsEffect}, {@code DestroyEachTargetPermanentEffect},
 *       {@code DestroyTargetPermanentsWithinTotalManaValueEffect}) {@code DepartureResult}s, those that stayed included,
 *       keep each one's last-known controller, power, toughness and mana value and where it went (whether it died); a
 *       removed permanent's {@link StepOutcome#movedCards()} name the card it became in the zone it went to; an
 *       {@link #EXILE} step that exiles cards from a graveyard, a library or a hand records those cards and their
 *       owners, and how many came from a hand ({@link StepOutcome#fromHand()}) where the step exiles from a hand among
 *       other zones;</li>
 *   <li>{@link #RETURN}: the number of permanents or cards returned, the permanents (to a hand: by their id as the step
 *       acted on them; to the battlefield: the permanent each card became, the exile-and-return-transformed of the
 *       source included; what {@code PutCardsOntoBattlefieldEffect} put there too) or cards,
 *       their owners; what went to a hand has its {@link StepOutcome#movedCards()} there
 *       (read by perpetual riders only), and a permanent returned to a hand its last-known stats
 *       ({@link StepOutcome#lastKnownPermanents()});</li>
 *   <li>{@link #CREATE_TOKEN}: the number of tokens, the tokens, the players who got them;</li>
 *   <li>{@link #DEAL_DAMAGE}: the total damage dealt, the permanents dealt damage, the players dealt damage;</li>
 *   <li>{@link #GAIN_LIFE}, {@link #LOSE_LIFE}: the total life gained or lost, nothing, the players;</li>
 *   <li>{@link #PUT_COUNTERS}, {@link #REMOVE_COUNTERS}: the number of counters, the permanents, their controllers;</li>
 *   <li>{@link #COUNTER_SPELL}, {@link #COPY_SPELL}: 1 when the spell was countered or copied, else 0; a countered
 *       spell's {@link StepOutcome#movedCards()} name the card it became in its graveyard or in exile;</li>
 *   <li>{@link #MANIFEST}: the number of permanents manifested, those permanents, their controllers;</li>
 *   <li>{@link #TRANSFORM}: 1 when the source transformed (or converted), else 0; that permanent, its controller;</li>
 *   <li>{@link #SEEK}: the number of cards sought into a hand ({@code SeekCardsToHandEffect}), those cards, the
 *       player; a sought card is followed into the hand it went to, as seek's cards are (digital only);</li>
 *   <li>{@link #CHOOSE}: the number of cards chosen without moving them ({@code ChooseCardEffect}, a revealed-hand
 *       choice that keeps the card in the hand), those cards, their owners, each one's {@link StepOutcome#movedCards()}
 *       entry naming the zone and stay it was chosen in; or of a player chosen at random
 *       ({@code ChoosePlayerAtRandomEffect}) or by the controller ({@code ChoosePlayerEffect}), that player; or of
 *       permanents ({@code ChoosePermanentsEffect}): the number chosen, those permanents, their controllers; or of the
 *       chosen piles of a pile separation ({@code SeparateIntoPilesEffect}): those permanents and their controllers, or
 *       those cards, their owners and each one's {@link StepOutcome#movedCards()} entry;</li>
 *   <li>{@link #PUT_INTO_GRAVEYARD}: the number of exiled cards put into a graveyard ({@code
 *       PutExiledCardsIntoOwnersGraveyardEffect}; a card a replacement sent elsewhere is not counted), those cards,
 *       their owners;</li>
 *   <li>{@link #REVEAL}: the number of cards revealed from the top of libraries
 *       ({@code RevealTopCardOfLibraryEffect}, {@code RevealCardsFromTopUntilEffect}) or shown from hands
 *       ({@code RevealCardsFromHandEffect}, a look included, CR 701.20e), those cards, their players, each one's
 *       {@link StepOutcome#movedCards()} entry naming its player's library or hand, which revealing doesn't make it
 *       leave (CR 701.20b);</li>
 *   <li>{@link #SHUFFLE_INTO_LIBRARY}: the number of cards and permanents shuffled into libraries
 *       ({@code EachPlayerShufflesZonesIntoLibraryEffect}; a token counts, though it ceases to exist there, CR 111.7),
 *       nothing, the players who shuffled some, and how many were each player's
 *       ({@link StepOutcome#amountByPlayer()});</li>
 *   <li>{@link #PAY_MANA}: the total mana paid ({@code EachPlayerPaysAnyManaEffect}), nothing, the players who paid
 *       some, and how much each paid ({@link StepOutcome#amountByPlayer()});</li>
 *   <li>{@link #PUT_INTO_LIBRARY}: the number of cards put into libraries on top or on the bottom
 *       ({@code PutCardsIntoLibraryEffect}), those cards, their owners: a hidden zone, so no card subject names them
 *       there.</li>
 *   <li>{@link #NOTE}: the number noted ({@code NoteAmountEffect}: "note its power", "note that excess damage"),
 *       nothing, no player; a delayed ability the same resolution makes keeps it ("where X is the noted number",
 *       {@code TemporaryGlobalTriggeredAbility.keepingNotedAmount}).</li>
 *   <li>{@link #GAIN_CONTROL}: the number of permanents whose controller the step changed
 *       ({@code GainControlOfTargetEffect}; one the gaining player already controlled is not counted), those
 *       permanents, the gaining player.</li>
 *   <li>{@link #VOTE}: the number of votes cast ({@code VoteEffect}: an additional vote counts, CR 701.38d), nothing,
 *       the voters in the order they voted, and each vote, who cast it and for what
 *       ({@link StepOutcome#votes()}): "for each [option] vote" ({@code VotesFor}), "if [option] gets more votes"
 *       ({@code VoteResult}).</li>
 *   <li>{@link #CONJURE}: the number of cards conjured into a hand, a library, a graveyard or exile
 *       ({@code ConjureCardsEffect}), those cards, the player whose zone they went to, each one's
 *       {@link StepOutcome#movedCards()} entry naming the zone it is in after the step (a replacement's zone, when one
 *       sent it elsewhere), so "you may cast it" and "it perpetually gets" find it there (digital only).</li>
 *   <li>{@link #TAP}: the number of permanents the step tapped ({@code TapPermanentsEffect}; one already tapped, or
 *       one that can't become tapped, is not counted), those permanents, their controllers: "for each creature tapped
 *       this way", "each of those Vampires".</li>
 *   <li>{@link #GOAD}: the number of creatures the step goaded ({@code GoadTargetCreatureUntilNextTurnEffect}; one the
 *       same player goaded again counts, CR 701.15d), those creatures, their controllers: "for each creature goaded this
 *       way".</li>
 *   <li>{@link #SEARCH}: the number of cards a library search found ({@code SearchLibraryEffect},
 *       {@code SearchTargetLibraryEffect}, {@code IntuitionEffect}), those cards, the searching player, each one's
 *       {@link StepOutcome#movedCards()} entry naming the zone the search put it in (a hand, a graveyard, exile, or the
 *       library for a search that reveals and keeps it there, CR 701.20b); a card put onto the battlefield has no entry
 *       ({@code Subjects.searched()} names the permanent). A search that is prevented, finds nothing or searches an
 *       empty library records nothing found (CR 701.23a, 701.23b); a card Opposition Agent exiled instead is not
 *       found this way.</li>
 * </ul>
 *
 * <p>A later step may read a step only if that step finishes before the next one starts (CR 608.2c): its handler never
 * prompts, pauses by resolving itself again before it changes anything, or pauses at the end of its step with its
 * result recorded by then. The recording handlers: draw, gain and lose life, exile a permanent (also until the source leaves) or each matching permanent
 * ({@code ExileAllPermanentsEffect}), exile the source's or
 * the triggering card or target cards from a graveyard ({@code ExileGraveyardCardsEffect} except where its player
 * chooses at a prompt), exile the top card of a library, a hand or a spell ({@code ExileTargetPlayerHandEffect},
 * {@code ExileTargetSpellEffect}), seek cards to a hand, conjure cards ({@code ConjureCardsEffect}), put exiled cards into a graveyard, note a number, destroy, copy, create a token (also attached to a permanent, {@code CreateTokenAttachedToTargetEffect}, whose choice among triggering creatures adds the token as it is answered), manifest, mill and exile the
 * source and return it transformed ({@code ExileSelfAndReturnTransformedEffect}), transform the source
 * ({@code TransformSelfEffect}) (never prompt, or
 * rerun before any change); the self sacrifice ({@code departAsStep}), discard and
 * gain control ({@code GainControlOfTargetEffect}, never prompting), sacrifice (their prompts add to the result as they are answered, through {@code GameData.discarded} and
 * {@code DestructionSupport.sacrificeTogether}), the chosen return to hand
 * ({@code ReturnPermanentControlledByPlayerToHandEffect}: its {@code BounceCreature} choice adds the returned
 * permanent; {@code ReturnChosenPermanentsToHandEffect}: its answer adds all of them), put counters (a replacement order answered later adds the rest; on each matching permanent,
 * {@code PutCounterOnEachMatchingPermanentEffect}, never prompting) and counter a spell; the chosen-permanent exile ({@code ExileChosenPermanentsEffect}: its
 * choices add to the result as they are answered), the chosen hand-card exile ({@code ExileCardFromHandEffect}), the
 * same-name exile ({@code ExileCardsWithSameNameEffect}), the card and random-player choices
 * ({@code ChooseCardEffect}, {@code ChoosePlayerAtRandomEffect}), the revealed-hand choice's exile and keep
 * ({@code ChooseCardsFromTargetHandEffect}), the pay-X token step ({@code PayXManaCreateXTokensEffect}), the reveal of
 * the top cards of libraries and from hands ({@code RevealTopCardOfLibraryEffect},
 * {@code RevealCardsFromTopUntilEffect}, {@code RevealCardsFromHandEffect}, never prompting), the discard of named
 * cards ({@code DiscardCardsEffect}: a replacement's question ends it) and the any-amount payment
 * ({@code EachPlayerPaysAnyManaEffect}: it resolves again at its own index for each answer and records after the last),
 * the tap ({@code TapPermanentsEffect}: its up-to-N choice adds what it taps as it is answered), the goad
 * ({@code GoadTargetCreatureUntilNextTurnEffect}, never prompting), the shuffle of each player's zones into their library
 * ({@code EachPlayerShufflesZonesIntoLibraryEffect}, never prompting), the vote ({@code VoteEffect}: it records as its last
 * vote is cast), the library search ({@code SearchLibraryEffect}, {@code SearchTargetLibraryEffect}: nothing as it begins,
 * each card as its pick is answered; {@code IntuitionEffect}: as its answer ends it), the put onto the battlefield
 * ({@code PutCardsOntoBattlefieldEffect}: its entry choices come before
 * anything moves and it records as its batch enters); deal damage, return a card from a graveyard,
 * remove counters and return to hand only on the paths that
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
    COPY_SPELL,
    MANIFEST,
    TRANSFORM,
    PUT_INTO_GRAVEYARD,
    SEEK,
    CHOOSE,
    REVEAL,
    PAY_MANA,
    SHUFFLE_INTO_LIBRARY,
    PUT_INTO_LIBRARY,
    GAIN_CONTROL,
    NOTE,
    VOTE,
    CONJURE,
    TAP,
    GOAD,
    SEARCH
}
