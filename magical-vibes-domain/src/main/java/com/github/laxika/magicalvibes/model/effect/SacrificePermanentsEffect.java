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
 */
public record SacrificePermanentsEffect(DynamicAmount count, PermanentPredicate filter,
        SacrificeRecipient recipient) implements CardEffect {

    /** Fixed count. */
    public SacrificePermanentsEffect(int count, PermanentPredicate filter, SacrificeRecipient recipient) {
        this(new Fixed(count), filter, recipient);
    }

    @Override
    public TargetSpec targetSpec() {
        // Only the target-player recipient targets a player; the kept validator enforces the
        // requireTargetPlayer guard the no-op PLAYER category cannot reproduce.
        return recipient == SacrificeRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetCategory.PLAYER)
                : TargetSpec.NONE;
    }
}
