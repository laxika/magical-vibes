package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

/**
 * Cumulative upkeep [cost] (CR 702.24): at the beginning of your upkeep, put an age counter on this
 * permanent, then you may pay {@code cost} for each age counter on it. If you don't, sacrifice it.
 *
 * <p>Mana costs use {@link #costPerAge} (payment flagged so cumulative-upkeep-only mana works).
 * Optional {@link #lifePerAge} adds life paid per age counter alongside the mana (Infernal Darkness
 * — "Pay {B} and 1 life"); an empty {@link #costPerAge} makes the cost life-only (Glacial Chasm —
 * "Pay 2 life"). Non-mana costs use {@link #sacrificeFilter}: sacrifice one matching permanent per
 * age counter (Polar Kraken — "Sacrifice a land") or {@link #exileTopCardsPerAge}: exile one card
 * from the top of the controller's library per age counter (Thought Lash), or
 * {@link #opponentTokenPerAge}: have an opponent create one token per age counter (Varchild's
 * War-Riders), or {@link #drawCardsPerAge}: draw one card per age counter (Psychic Vortex), or
 * {@link #counterTypePerAge}: put one counter of that type on the permanent itself per age counter
 * (Aboroth — "Put a -1/-1 counter on this creature").
 * {@link #opponentCreatureCounterTypePerAge}: put one counter of that type on a creature an
 * opponent controls per age counter (Sheltering Ancient).
 *
 * <p>{@link #unpaidEffects} models a companion "when a player doesn't pay this permanent's
 * cumulative upkeep …" triggered ability (Thought Lash): those effects resolve alongside the
 * sacrifice when the cost is declined or cannot be paid.
 *
 * @param costPerAge mana cost string paid once per age counter (e.g. {@code "{U}"}), empty for a
 *     life-only cost, or null when the cost is a sacrifice or a library exile
 * @param sacrificeFilter permanent filter for a sacrifice-per-age-counter cost, or null for mana
 * @param gainControlFilter permanent filter for a gain-control-per-age-counter cost, or null for
 *     other costs
 * @param lifePerAge life paid once per age counter (0 when the cost is mana-only or sacrifice)
 * @param exileTopCardsPerAge when true the cost is "exile the top card of your library" per age
 *     counter; mutually exclusive with the mana and sacrifice costs
 * @param opponentTokenPerAge blueprint for a token an opponent creates once per age counter, or
 *     null; mutually exclusive with every other cost
 * @param drawCardsPerAge when true the cost is "draw a card" per age counter; mutually exclusive
 *     with every other cost
 * @param counterTypePerAge counter put on the permanent itself once per age counter, or null;
 *     mutually exclusive with every other cost
 * @param discardCardsPerAge when true, discard one card per age counter; mutually exclusive with
 *     every other cost
 * @param putCardsFromSingleGraveyardPerAge cards chosen from one graveyard for each age-counter
 *     payment, or 0 when unused; mutually exclusive with every other cost
 * @param opponentLifeGainPerAge life gained by an opponent once per age counter, or 0 when unused;
 *     mutually exclusive with every other cost
 * @param unpaidEffects extra effects resolved when the cost isn't paid, on top of the sacrifice
 * @param paidEffects extra effects resolved when the mana cost is paid
 * @param flipCoinPerAge when true, flip one coin per age counter instead of paying a mana or
 *     permanent-based cost
 * @param opponentCreatureCounterTypePerAge counter put on an opponent's creature once per age
 *     counter, or null for other costs
 */
public record CumulativeUpkeepEffect(
        String costPerAge,
        PermanentPredicate sacrificeFilter,
        PermanentPredicate gainControlFilter,
        int lifePerAge,
        boolean exileTopCardsPerAge,
        CreateTokenEffect opponentTokenPerAge,
        boolean drawCardsPerAge,
        CounterType counterTypePerAge,
        boolean discardCardsPerAge,
        int putCardsFromSingleGraveyardPerAge,
        int opponentLifeGainPerAge,
        List<CardEffect> unpaidEffects,
        List<CardEffect> paidEffects,
        boolean flipCoinPerAge,
        CounterType opponentCreatureCounterTypePerAge)
        implements CardEffect {

    public CumulativeUpkeepEffect {
        unpaidEffects = unpaidEffects == null ? List.of() : List.copyOf(unpaidEffects);
        paidEffects = paidEffects == null ? List.of() : List.copyOf(paidEffects);
        if (flipCoinPerAge) {
            if (costPerAge != null || sacrificeFilter != null || gainControlFilter != null || lifePerAge != 0
                    || exileTopCardsPerAge || opponentTokenPerAge != null || drawCardsPerAge
                    || counterTypePerAge != null || discardCardsPerAge || putCardsFromSingleGraveyardPerAge > 0
                    || opponentLifeGainPerAge > 0 || opponentCreatureCounterTypePerAge != null
                    || !paidEffects.isEmpty()) {
                throw new IllegalArgumentException("A coin-flip cumulative upkeep takes no other cost");
            }
        } else if (opponentCreatureCounterTypePerAge != null) {
            if (costPerAge != null || sacrificeFilter != null || gainControlFilter != null || lifePerAge != 0
                    || exileTopCardsPerAge || opponentTokenPerAge != null || drawCardsPerAge
                    || counterTypePerAge != null || discardCardsPerAge || putCardsFromSingleGraveyardPerAge > 0
                    || opponentLifeGainPerAge > 0 || !paidEffects.isEmpty()) {
                throw new IllegalArgumentException(
                        "An opponent-creature-counter cumulative upkeep takes no other cost");
            }
        } else if (opponentLifeGainPerAge > 0) {
            if (costPerAge != null || sacrificeFilter != null || lifePerAge != 0 || exileTopCardsPerAge
                    || opponentTokenPerAge != null || drawCardsPerAge || counterTypePerAge != null
                    || discardCardsPerAge || putCardsFromSingleGraveyardPerAge > 0
                    || gainControlFilter != null) {
                throw new IllegalArgumentException(
                        "An opponent-life-gain cumulative upkeep takes no other cost");
            }
        } else if (putCardsFromSingleGraveyardPerAge > 0) {
            if (costPerAge != null || sacrificeFilter != null || lifePerAge != 0 || exileTopCardsPerAge
                    || opponentTokenPerAge != null || drawCardsPerAge || counterTypePerAge != null
                    || discardCardsPerAge || gainControlFilter != null) {
                throw new IllegalArgumentException(
                        "A put-cards-from-a-graveyard cumulative upkeep takes no other cost");
            }
        } else if (discardCardsPerAge) {
            if (costPerAge != null || sacrificeFilter != null || lifePerAge != 0 || exileTopCardsPerAge
                    || opponentTokenPerAge != null || drawCardsPerAge || counterTypePerAge != null
                    || gainControlFilter != null) {
                throw new IllegalArgumentException(
                        "A discard-card cumulative upkeep takes no other cost");
            }
        } else if (counterTypePerAge != null) {
            if (costPerAge != null || sacrificeFilter != null || lifePerAge != 0 || exileTopCardsPerAge
                    || opponentTokenPerAge != null || drawCardsPerAge || gainControlFilter != null) {
                throw new IllegalArgumentException(
                        "A put-a-counter cumulative upkeep takes no other cost");
            }
        } else if (drawCardsPerAge) {
            if (costPerAge != null || sacrificeFilter != null || lifePerAge != 0 || exileTopCardsPerAge
                    || opponentTokenPerAge != null || gainControlFilter != null) {
                throw new IllegalArgumentException(
                        "A draw-a-card cumulative upkeep takes no other cost");
            }
        } else if (opponentTokenPerAge != null) {
            if (costPerAge != null || sacrificeFilter != null || lifePerAge != 0 || exileTopCardsPerAge
                    || gainControlFilter != null) {
                throw new IllegalArgumentException(
                        "An opponent-token cumulative upkeep takes no other cost");
            }
        } else if (exileTopCardsPerAge) {
            if (costPerAge != null || sacrificeFilter != null || lifePerAge != 0 || gainControlFilter != null) {
                throw new IllegalArgumentException(
                        "An exile-top-card cumulative upkeep takes no mana, life or sacrifice cost");
            }
        } else if (gainControlFilter != null) {
            if (costPerAge != null || sacrificeFilter != null || lifePerAge != 0 || exileTopCardsPerAge
                    || opponentTokenPerAge != null || drawCardsPerAge || counterTypePerAge != null
                    || discardCardsPerAge || putCardsFromSingleGraveyardPerAge > 0) {
                throw new IllegalArgumentException("A gain-control cumulative upkeep takes no other cost");
            }
        } else if ((costPerAge == null) == (sacrificeFilter == null)) {
            throw new IllegalArgumentException(
                    "Exactly one of costPerAge or sacrificeFilter must be non-null");
        }
        if (lifePerAge < 0) {
            throw new IllegalArgumentException("lifePerAge must be >= 0");
        }
        if (putCardsFromSingleGraveyardPerAge < 0) {
            throw new IllegalArgumentException("putCardsFromSingleGraveyardPerAge must be >= 0");
        }
        if (opponentLifeGainPerAge < 0) {
            throw new IllegalArgumentException("opponentLifeGainPerAge must be >= 0");
        }
        if (sacrificeFilter != null && lifePerAge > 0) {
            throw new IllegalArgumentException("lifePerAge is only valid with a mana costPerAge");
        }
        if (costPerAge != null && costPerAge.isEmpty() && lifePerAge <= 0) {
            throw new IllegalArgumentException("An empty costPerAge requires a positive lifePerAge");
        }
    }

    /** Cumulative upkeep — Pay N life (Glacial Chasm); no mana component. */
    public static CumulativeUpkeepEffect life(int lifePerAge) {
        return new CumulativeUpkeepEffect("", null, lifePerAge, false, null, false, null, false, 0, 0,
                List.of(), List.of());
    }

    /** Cumulative upkeep {mana} — e.g. {@code new CumulativeUpkeepEffect("{1}")}. */
    public CumulativeUpkeepEffect(String costPerAge) {
        this(costPerAge, null, 0, false, null, false, null, false, 0, 0, List.of(), List.of());
    }

    /** Cumulative upkeep — Pay {mana} and N life (Infernal Darkness). */
    public CumulativeUpkeepEffect(String costPerAge, int lifePerAge) {
        this(costPerAge, null, lifePerAge, false, null, false, null, false, 0, 0, List.of(), List.of());
    }

    /** Compatibility constructor for the pre-gain-control cumulative-upkeep shape. */
    public CumulativeUpkeepEffect(String costPerAge, PermanentPredicate sacrificeFilter, int lifePerAge,
            boolean exileTopCardsPerAge, CreateTokenEffect opponentTokenPerAge, boolean drawCardsPerAge,
            CounterType counterTypePerAge, boolean discardCardsPerAge, int putCardsFromSingleGraveyardPerAge,
            int opponentLifeGainPerAge, List<CardEffect> unpaidEffects, List<CardEffect> paidEffects) {
        this(costPerAge, sacrificeFilter, null, lifePerAge, exileTopCardsPerAge, opponentTokenPerAge,
                drawCardsPerAge, counterTypePerAge, discardCardsPerAge, putCardsFromSingleGraveyardPerAge,
                opponentLifeGainPerAge, unpaidEffects, paidEffects, false, null);
    }

    /** Compatibility constructor for the current full cumulative-upkeep shape. */
    public CumulativeUpkeepEffect(String costPerAge, PermanentPredicate sacrificeFilter,
            PermanentPredicate gainControlFilter, int lifePerAge, boolean exileTopCardsPerAge,
            CreateTokenEffect opponentTokenPerAge, boolean drawCardsPerAge, CounterType counterTypePerAge,
            boolean discardCardsPerAge, int putCardsFromSingleGraveyardPerAge, int opponentLifeGainPerAge,
            List<CardEffect> unpaidEffects, List<CardEffect> paidEffects) {
        this(costPerAge, sacrificeFilter, gainControlFilter, lifePerAge, exileTopCardsPerAge,
                opponentTokenPerAge, drawCardsPerAge, counterTypePerAge, discardCardsPerAge,
                putCardsFromSingleGraveyardPerAge, opponentLifeGainPerAge, unpaidEffects, paidEffects, false, null);
    }

    /**
     * Cumulative upkeep {mana}, with extra effects that resolve when the cost isn't paid — the
     * "when a player doesn't pay this permanent's cumulative upkeep, …" companion trigger
     * (Heart of Bogardan).
     */
    public static CumulativeUpkeepEffect withUnpaidEffects(String costPerAge, List<CardEffect> unpaidEffects) {
        return new CumulativeUpkeepEffect(costPerAge, null, 0, false, null, false, null, false, 0, 0,
                unpaidEffects, List.of());
    }

    /** Cumulative upkeep with effects that trigger after the mana cost is paid. */
    public static CumulativeUpkeepEffect withPaidEffects(String costPerAge, List<CardEffect> paidEffects) {
        return new CumulativeUpkeepEffect(costPerAge, null, 0, false, null, false, null, false, 0, 0,
                List.of(), paidEffects);
    }

    /** Cumulative upkeep — sacrifice a permanent matching {@code filter} per age counter. */
    public static CumulativeUpkeepEffect sacrifice(PermanentPredicate filter) {
        return new CumulativeUpkeepEffect(null, filter, 0, false, null, false, null, false, 0, 0,
                List.of(), List.of());
    }

    /** Cumulative upkeep — gain control of a matching permanent per age counter. */
    public static CumulativeUpkeepEffect gainControlOf(PermanentPredicate filter) {
        return new CumulativeUpkeepEffect(null, null, filter, 0, false, null, false, null, false,
                0, 0, List.of(), List.of());
    }

    /** Cumulative upkeep — Draw a card (Psychic Vortex); one card per age counter. */
    public static CumulativeUpkeepEffect drawCard() {
        return new CumulativeUpkeepEffect(null, null, 0, false, null, true, null, false, 0, 0,
                List.of(), List.of());
    }

    /**
     * Cumulative upkeep — Exile the top card of your library, with extra effects that resolve when
     * the cost isn't paid (Thought Lash).
     */
    public static CumulativeUpkeepEffect exileTopCard(List<CardEffect> unpaidEffects) {
        return new CumulativeUpkeepEffect(null, null, 0, true, null, false, null, false, 0, 0,
                unpaidEffects, List.of());
    }

    /**
     * Cumulative upkeep — Have an opponent create one {@code token} per age counter (Varchild's
     * War-Riders).
     */
    public static CumulativeUpkeepEffect opponentToken(CreateTokenEffect token) {
        return new CumulativeUpkeepEffect(null, null, 0, false, token, false, null, false, 0, 0,
                List.of(), List.of());
    }

    /**
     * Cumulative upkeep — put one counter of {@code counterType} on this permanent per age counter
     * (Aboroth — "Put a -1/-1 counter on this creature").
     */
    public static CumulativeUpkeepEffect putCounterOnSelf(CounterType counterType) {
        return new CumulativeUpkeepEffect(null, null, 0, false, null, false, counterType, false, 0, 0,
                List.of(), List.of());
    }

    /** Cumulative upkeep — discard a card per age counter (Vexing Sphinx). */
    public static CumulativeUpkeepEffect discardCard() {
        return new CumulativeUpkeepEffect(null, null, 0, false, null, false, null, true, 0, 0,
                List.of(), List.of());
    }

    /** Cumulative upkeep — put cards from a single graveyard on library bottoms per age counter. */
    public static CumulativeUpkeepEffect putCardsFromSingleGraveyard(int cardsPerAge) {
        if (cardsPerAge <= 0) {
            throw new IllegalArgumentException("cardsPerAge must be positive");
        }
        return new CumulativeUpkeepEffect(null, null, 0, false, null, false, null, false,
                cardsPerAge, 0, List.of(), List.of());
    }

    /** Cumulative upkeep — have an opponent gain life once per age counter (Wall of Shards). */
    public static CumulativeUpkeepEffect opponentGainsLife(int lifePerAge) {
        if (lifePerAge <= 0) {
            throw new IllegalArgumentException("lifePerAge must be positive");
        }
        return new CumulativeUpkeepEffect(null, null, 0, false, null, false, null, false, 0,
                lifePerAge, List.of(), List.of());
    }

    /** Cumulative upkeep — flip a coin once per age counter (Karplusan Minotaur). */
    public static CumulativeUpkeepEffect flipCoin() {
        return new CumulativeUpkeepEffect(null, null, null, 0, false, null, false, null,
                false, 0, 0, List.of(), List.of(), true, null);
    }

    /** Cumulative upkeep — put a counter on a creature an opponent controls per age counter. */
    public static CumulativeUpkeepEffect putCounterOnOpponentCreature(CounterType counterType) {
        return new CumulativeUpkeepEffect(null, null, null, 0, false, null, false, null,
                false, 0, 0, List.of(), List.of(), false, counterType);
    }

    public boolean isSacrificeCost() {
        return sacrificeFilter != null;
    }
}
