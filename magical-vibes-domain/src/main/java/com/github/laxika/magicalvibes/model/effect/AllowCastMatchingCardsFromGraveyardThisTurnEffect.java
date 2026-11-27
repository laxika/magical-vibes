package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "You may cast [filtered] spells from your graveyard this turn" (Liliana, Untouched by Death's
 * −3). Unlike {@link AllowCastTargetCardFromGraveyardThisTurnEffect}, which permits one chosen
 * card, this grants a turn-scoped blanket permission for every matching card — including cards
 * that reach the graveyard later in the turn. Unlike a
 * {@link CastSpellsFromGraveyardPermission}, which is a static ability of a permanent on the
 * battlefield, this is a one-shot grant recorded on {@code GameData} and dropped at turn cleanup.
 *
 * <p>{@code graveyards} names whose graveyard the spells may be cast from: {@link Subjects#you()} by default, one
 * player ("from that player's graveyard", {@link Subjects#triggeringPlayer()}), or every player ("from any graveyard",
 * {@link Subjects#eachPlayer()}). {@code exileInsteadOfGraveyard} is "if a spell cast this way would be put into a
 * graveyard, exile it instead"; {@code manaAsThoughAnyColor} is "and you may spend mana as though it were mana of any
 * color to cast that spell", which leaves a {C} symbol to colorless mana (CR 107.4c).
 */
public record AllowCastMatchingCardsFromGraveyardThisTurnEffect(
        CardPredicate filter,
        boolean singleUse,
        CounterType entryCounterType,
        CardSubtype grantedSubtype,
        ForageCost additionalCost,
        CounterType enterWithCounter,
        int enterWithCounterCount,
        PlayerSubject graveyards,
        boolean exileInsteadOfGraveyard,
        boolean manaAsThoughAnyColor) implements CardEffect {

    public AllowCastMatchingCardsFromGraveyardThisTurnEffect {
        Objects.requireNonNull(graveyards, "graveyards");
    }

    public AllowCastMatchingCardsFromGraveyardThisTurnEffect(
            CardPredicate filter, boolean singleUse, CounterType entryCounterType, CardSubtype grantedSubtype,
            ForageCost additionalCost, CounterType enterWithCounter, int enterWithCounterCount) {
        this(filter, singleUse, entryCounterType, grantedSubtype, additionalCost, enterWithCounter,
                enterWithCounterCount, Subjects.you(), false, false);
    }

    /** The permission over the graveyards of {@code players} instead of yours. */
    public AllowCastMatchingCardsFromGraveyardThisTurnEffect fromGraveyardsOf(PlayerSubject players) {
        return new AllowCastMatchingCardsFromGraveyardThisTurnEffect(filter, singleUse, entryCounterType,
                grantedSubtype, additionalCost, enterWithCounter, enterWithCounterCount, players,
                exileInsteadOfGraveyard, manaAsThoughAnyColor);
    }

    /** "If a spell cast this way would be put into a graveyard, exile it instead." */
    public AllowCastMatchingCardsFromGraveyardThisTurnEffect exilingInsteadOfGraveyard() {
        return new AllowCastMatchingCardsFromGraveyardThisTurnEffect(filter, singleUse, entryCounterType,
                grantedSubtype, additionalCost, enterWithCounter, enterWithCounterCount, graveyards, true,
                manaAsThoughAnyColor);
    }

    /** "… and you may spend mana as though it were mana of any color to cast that spell." */
    public AllowCastMatchingCardsFromGraveyardThisTurnEffect spendingManaAsThoughAnyColor() {
        return new AllowCastMatchingCardsFromGraveyardThisTurnEffect(filter, singleUse, entryCounterType,
                grantedSubtype, additionalCost, enterWithCounter, enterWithCounterCount, graveyards,
                exileInsteadOfGraveyard, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return graveyards.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    public AllowCastMatchingCardsFromGraveyardThisTurnEffect(CardPredicate filter) {
        this(filter, false, null, null, null, null, 0);
    }

    public AllowCastMatchingCardsFromGraveyardThisTurnEffect(CardPredicate filter, boolean singleUse) {
        this(filter, singleUse, null, null, null, null, 0);
    }

    public AllowCastMatchingCardsFromGraveyardThisTurnEffect(
            CardPredicate filter, ForageCost additionalCost,
            CounterType enterWithCounter, int enterWithCounterCount) {
        this(filter, false, null, null, additionalCost, enterWithCounter, enterWithCounterCount);
    }

    public static AllowCastMatchingCardsFromGraveyardThisTurnEffect oneShotWithEntryEffects(
            CardPredicate filter, CounterType entryCounterType, CardSubtype grantedSubtype) {
        return new AllowCastMatchingCardsFromGraveyardThisTurnEffect(
                filter, true, entryCounterType, grantedSubtype, null, null, 0);
    }
}
