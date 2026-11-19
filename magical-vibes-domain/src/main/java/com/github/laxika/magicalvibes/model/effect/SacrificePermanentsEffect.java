package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Forces one or more players to sacrifice permanents. A single record covers the whole
 * forced-sacrifice family: the {@link PlayerSubject} names who sacrifices ({@code you()},
 * {@code targetPlayer()}, {@code eachPlayer()}, {@code defendingPlayer()}, ...), the
 * {@link PermanentPredicate} filter restricts which permanents are eligible, and the
 * {@link DynamicAmount} count covers fixed counts ("sacrifices five lands") or dynamic counts.
 *
 * <p>Two interaction mechanics are preserved for byte-identical behaviour with the pre-collapse
 * handlers, distinguished by the filter: a bare {@code PermanentIsCreaturePredicate} routes through
 * the single-select "sacrifice a creature" primitive ({@code PermanentChoiceContext.SacrificeCreature}
 * — the old {@code SacrificeCreatureEffect} / {@code ControllerSacrificesCreatureEffect} /
 * {@code EachOpponentSacrificesCreatureEffect}); any other filter routes through the multi-permanent
 * choice ({@code MultiPermanentChoiceContext.ForcedSacrifice} — the old
 * {@code TargetPlayerSacrificesPermanentsEffect} / {@code EachPlayerSacrificesPermanentsEffect} /
 * {@code EachOpponentSacrificesPermanentsEffect}). Both are behaviourally tested and rules-correct:
 * "sacrifice a creature" is always a single creature.
 *
 * <p>Example: "Each player sacrifices five lands." →
 * {@code new SacrificePermanentsEffect(5, new PermanentIsLandPredicate(), Subjects.eachPlayer())}
 *
 * <p>Several players choose in APNAP order and then sacrifice together (CR 101.4); a lone player
 * uses the single-player path.
 *
 * @param count     number of permanents to sacrifice
 * @param filter    which permanents are eligible
 * @param player    who sacrifices; declares a benign player target only for {@code targetPlayer()}
 * @param countPerSacrificingPlayer when {@code true} the count is evaluated once per sacrificing
 *                                  player, with player-relative scopes ({@code CountScope.CONTROLLER})
 *                                  reading that player's own permanents instead of the spell's
 *                                  controller — "each player sacrifices … for each white permanent
 *                                  <em>they</em> control" (Omen of Fire)
 * @param recordSacrificedCount when {@code true}, the actual number sacrificed by a direct
 *                              single-player resolution is stored on the stack entry for a following
 *                              {@code EventValue} amount
 */
public record SacrificePermanentsEffect(DynamicAmount count, PermanentPredicate filter,
        PlayerSubject player, boolean countPerSacrificingPlayer, boolean recordSacrificedCount,
        boolean recordSacrificedPower, boolean simultaneousChoices)
        implements CardEffect {

    public SacrificePermanentsEffect {
        java.util.Objects.requireNonNull(player, "player");
    }

    public SacrificePermanentsEffect(DynamicAmount count, PermanentPredicate filter,
            PlayerSubject player, boolean countPerSacrificingPlayer,
            boolean recordSacrificedCount, boolean recordSacrificedPower) {
        this(count, filter, player, countPerSacrificingPlayer, recordSacrificedCount,
                recordSacrificedPower, false);
    }

    public SacrificePermanentsEffect(DynamicAmount count, PermanentPredicate filter,
            PlayerSubject player, boolean countPerSacrificingPlayer,
            boolean recordSacrificedCount) {
        this(count, filter, player, countPerSacrificingPlayer, recordSacrificedCount, false, false);
    }

    /** Count evaluated once, from the spell's controller's perspective. */
    public SacrificePermanentsEffect(DynamicAmount count, PermanentPredicate filter,
            PlayerSubject player) {
        this(count, filter, player, false, false, false, false);
    }

    /** Count evaluated separately for each sacrificing player. */
    public SacrificePermanentsEffect(DynamicAmount count, PermanentPredicate filter,
            PlayerSubject player, boolean countPerSacrificingPlayer) {
        this(count, filter, player, countPerSacrificingPlayer, false, false, false);
    }

    /** Fixed count. */
    public SacrificePermanentsEffect(int count, PermanentPredicate filter, PlayerSubject player) {
        this(new Fixed(count), filter, player, false, false, false, false);
    }

    /** Returns a copy that records the actual direct sacrifice count for a following effect. */
    public SacrificePermanentsEffect withRecordedSacrificeCount() {
        return new SacrificePermanentsEffect(count, filter, player, countPerSacrificingPlayer, true,
                recordSacrificedPower, simultaneousChoices);
    }

    /** Returns a copy that records the sacrificed permanent's effective power for a following effect. */
    public SacrificePermanentsEffect withRecordedSacrificedPower() {
        return new SacrificePermanentsEffect(count, filter, player, countPerSacrificingPlayer,
                recordSacrificedCount, true, simultaneousChoices);
    }

    public SacrificePermanentsEffect withSimultaneousChoices() {
        return new SacrificePermanentsEffect(count, filter, player, countPerSacrificingPlayer,
                recordSacrificedCount, recordSacrificedPower, true);
    }

    @Override
    public TargetSpec targetSpec() {
        // Only a declared target player targets; the kept validator enforces the requireTargetPlayer
        // guard the no-op PLAYER category cannot reproduce.
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
