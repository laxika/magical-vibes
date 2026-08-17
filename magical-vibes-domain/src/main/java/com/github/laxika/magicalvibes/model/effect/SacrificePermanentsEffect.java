package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Forces one or more players to sacrifice permanents. A single record covers the whole
 * forced-sacrifice family: the {@link SacrificeRecipient} routes who sacrifices (controller /
 * target player / each player / each opponent), the {@link PermanentPredicate} filter restricts
 * which permanents are eligible, and the {@link DynamicAmount} count covers fixed counts
 * ("sacrifices five lands") or dynamic counts.
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
 * {@code new SacrificePermanentsEffect(5, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER)}
 *
 * @param count     number of permanents to sacrifice
 * @param filter    which permanents are eligible
 * @param recipient who sacrifices
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
        SacrificeRecipient recipient, boolean countPerSacrificingPlayer, boolean recordSacrificedCount)
        implements CardEffect, CombatDamageTriggerContextEffect {

    /** Count evaluated once, from the spell's controller's perspective. */
    public SacrificePermanentsEffect(DynamicAmount count, PermanentPredicate filter,
            SacrificeRecipient recipient) {
        this(count, filter, recipient, false, false);
    }

    /** Count evaluated separately for each sacrificing player. */
    public SacrificePermanentsEffect(DynamicAmount count, PermanentPredicate filter,
            SacrificeRecipient recipient, boolean countPerSacrificingPlayer) {
        this(count, filter, recipient, countPerSacrificingPlayer, false);
    }

    /** Fixed count. */
    public SacrificePermanentsEffect(int count, PermanentPredicate filter, SacrificeRecipient recipient) {
        this(new Fixed(count), filter, recipient, false, false);
    }

    /** Returns a copy that records the actual direct sacrifice count for a following effect. */
    public SacrificePermanentsEffect withRecordedSacrificeCount() {
        return new SacrificePermanentsEffect(count, filter, recipient, countPerSacrificingPlayer, true);
    }

    @Override
    public TargetSpec targetSpec() {
        // Only the target-player recipient targets a player; the kept validator enforces the
        // requireTargetPlayer guard the no-op PLAYER category cannot reproduce.
        return recipient == SacrificeRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        // "Whenever this creature deals combat damage to a player, that player sacrifices ..."
        // (Akki Underminer): the damaged player is bound as the trigger's targetId so the
        // TARGET_PLAYER recipient resolves without a targeting pipeline. Every other recipient
        // derives its sacrificers from the controller and needs no bound player.
        return recipient == SacrificeRecipient.TARGET_PLAYER ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
