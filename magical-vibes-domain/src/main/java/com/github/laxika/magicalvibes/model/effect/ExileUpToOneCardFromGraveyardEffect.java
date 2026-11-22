package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "You may exile a [creature] card from a graveyard": a non-targeting, resolution-time choice over every card in every
 * graveyard, or in those of the players {@code graveyardOf} names ("from your graveyard", "from that player's
 * graveyard"), that matches {@code filter} ({@code null} for any card). The controller chooses up to one card to exile
 * (one with {@code mandatory}, "exile a card from a graveyard"); when they must exile one from their own graveyard and
 * only one card matches, it is exiled without asking. Because it targets nothing at stack time it rides on
 * any resolution path.
 *
 * <p>It is an EXILE step: the handler asks, resolves itself again with the answer before anything changes, and records
 * the exiled card (and its graveyard's owner), {@code StepOutcome.NONE} when nothing was exiled, so "if you do" is
 * {@code HappenedThisWay.exiled()} and "if a creature card was exiled this way" is
 * {@code HappenedThisWay.toACard(StepKind.EXILE, filter)} (Entrails Feaster, Deathbonnet Hulk).
 *
 * @param mandatory       "exile a card from a graveyard": exactly one card when any matches
 * @param filter          which cards may be chosen; {@code null} for any card
 * @param trackWithSource the exiled card is tracked with the source permanent, for its abilities that read "cards
 *                        exiled with this creature" (CR 607.2a; the Cemetery cycle: {@link #withSource()})
 * @param graveyardOf     whose graveyards are offered; {@code null} for every graveyard. {@code targetPlayer()}
 *                        declares the player target (Hama, the Bloodbender; Dreams of Steel and Oil)
 */
public record ExileUpToOneCardFromGraveyardEffect(boolean mandatory, CardPredicate filter, boolean trackWithSource,
                                                  PlayerSubject graveyardOf)
        implements CardEffect {

    public ExileUpToOneCardFromGraveyardEffect(boolean mandatory, CardPredicate filter, boolean trackWithSource) {
        this(mandatory, filter, trackWithSource, null);
    }

    public ExileUpToOneCardFromGraveyardEffect() {
        this(false, null, false);
    }

    public ExileUpToOneCardFromGraveyardEffect(boolean mandatory) {
        this(mandatory, null, false);
    }

    /** "You may exile a [filter] card from a graveyard." */
    public static ExileUpToOneCardFromGraveyardEffect matching(CardPredicate filter) {
        return new ExileUpToOneCardFromGraveyardEffect(false, filter, false);
    }

    /** This exile, tracked with the source permanent ("cards exiled with this creature"). */
    public ExileUpToOneCardFromGraveyardEffect withSource() {
        return new ExileUpToOneCardFromGraveyardEffect(mandatory, filter, true, graveyardOf);
    }

    /** "Exile a [filter] card from a graveyard": a card must be exiled when one matches. */
    public static ExileUpToOneCardFromGraveyardEffect oneMatching(CardPredicate filter) {
        return new ExileUpToOneCardFromGraveyardEffect(true, filter, false);
    }

    /** This exile, offering only the graveyards of {@code players} ("from your graveyard", "from their graveyard"). */
    public ExileUpToOneCardFromGraveyardEffect from(PlayerSubject players) {
        return new ExileUpToOneCardFromGraveyardEffect(mandatory, filter, trackWithSource,
                Objects.requireNonNull(players, "players"));
    }

    @Override
    public TargetSpec targetSpec() {
        return graveyardOf != null && graveyardOf.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
