package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Each player {@code players} names (every player by default) returns up to {@code maxCount} cards matching
 * {@code filter} from their graveyard to the battlefield. Players choose in APNAP order, then every chosen card enters
 * at once (CR 101.4), under the control of the player who returned it (CR 110.2a) or, with {@code underOwnersControl},
 * of its owner. If a player has fewer matching cards than {@code maxCount}, they return all of them. When
 * {@code enterWithCounter} is non-null, each card returned this way enters with one counter of that type on it.
 *
 * <p>Example: "Each player returns up to two land cards from their graveyard to the battlefield."
 * → {@code new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(2, new CardTypePredicate(CardType.LAND))}
 *
 * <p>Example (Pyrrhic Revival): "Each player returns each creature card from their graveyard to
 * the battlefield with an additional -1/-1 counter on it."
 * → {@code new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(Integer.MAX_VALUE,
 *   new CardTypePredicate(CardType.CREATURE), CounterType.MINUS_ONE_MINUS_ONE)}
 *
 * <p>Example (Tempt with Immortality): "Return a creature card from your graveyard to the battlefield" is
 * {@code mandatory(1, creature).by(Subjects.you())}; "each opponent who accepted returns one" is the same over
 * {@code Subjects.acceptedPlayers()}.
 *
 * <p>Use the {@code fromBattlefieldThisTurn} overload for effects such as Second Sunrise that
 * return only cards put into the graveyard from the battlefield this turn.
 * When {@code enterTapped} is true, cards returned this way enter tapped.
 *
 * @param players who returns cards from their own graveyard; never a declared target
 */
public record EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
        int maxCount,
        CardPredicate filter,
        CounterType enterWithCounter,
        boolean fromBattlefieldThisTurn,
        boolean enterTapped,
        boolean underOwnersControl,
        boolean mandatory,
        PlayerSubject players
) implements CardEffect {

    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect {
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("Each player's graveyard return declares no target: " + players);
        }
    }

    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int maxCount, CardPredicate filter,
            CounterType enterWithCounter, boolean fromBattlefieldThisTurn, boolean enterTapped,
            boolean underOwnersControl, boolean mandatory) {
        this(maxCount, filter, enterWithCounter, fromBattlefieldThisTurn, enterTapped, underOwnersControl, mandatory,
                Subjects.eachPlayer());
    }

    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int maxCount, CardPredicate filter,
            CounterType enterWithCounter, boolean fromBattlefieldThisTurn, boolean enterTapped,
            boolean underOwnersControl) {
        this(maxCount, filter, enterWithCounter, fromBattlefieldThisTurn, enterTapped, underOwnersControl, false);
    }

    /** "Each player returns [exactly] {@code count} [filter] card[s]": a player with more must choose that many. */
    public static EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect mandatory(int count, CardPredicate filter) {
        return new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(count, filter, null,
                false, false, false, true);
    }

    /** The same return, done only by the players {@code returners} names ("return a creature card from your graveyard"). */
    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect by(PlayerSubject returners) {
        return new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(maxCount, filter, enterWithCounter,
                fromBattlefieldThisTurn, enterTapped, underOwnersControl, mandatory, returners);
    }

    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int maxCount, CardPredicate filter,
            CounterType enterWithCounter, boolean fromBattlefieldThisTurn, boolean enterTapped) {
        this(maxCount, filter, enterWithCounter, fromBattlefieldThisTurn, enterTapped, false);
    }

    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int maxCount, CardPredicate filter) {
        this(maxCount, filter, null, false, false);
    }

    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int maxCount, CardPredicate filter,
                                                                   CounterType enterWithCounter) {
        this(maxCount, filter, enterWithCounter, false, false);
    }

    public EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(int maxCount, CardPredicate filter,
                                                                   boolean fromBattlefieldThisTurn) {
        this(maxCount, filter, null, fromBattlefieldThisTurn, false);
    }
}
