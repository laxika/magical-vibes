package com.github.laxika.magicalvibes.model.effect;

/**
 * A player loses {@code lifeLoss} life unless they sacrifice a nonland permanent of their choice or
 * discard a card. The affected player picks one of the three outcomes; the offered options are pruned
 * to what they can actually do (losing life is always available), so a player who can neither sacrifice
 * nor discard just loses life without a prompt.
 *
 * <p>{@code recipient} selects who that player is:
 * <ul>
 *   <li>{@link LoseLifeRecipient#TARGET_PLAYER} — the player supplied on the stack entry's
 *       {@code targetId} (for a Curse upkeep trigger, the enchanted player baked by
 *       {@code StepTriggerService}). Torment of Scarabs.</li>
 *   <li>{@link LoseLifeRecipient#TARGET_PERMANENT_CONTROLLER} — the controller of the targeted
 *       permanent ({@code targetId} is a permanent, not a player). That permanent itself is excluded
 *       from the sacrifice options ("sacrifice <em>another</em> nonland permanent"). The effect adds
 *       no target of its own — pair it with a sibling targeting effect that owns the permanent target
 *       (e.g. {@link PutCounterOnTargetPermanentEffect}), listed first so the creature is still present.
 *       Torment of Venom.</li>
 * </ul>
 * Only these two recipients are supported.
 *
 * <p>The single-pass, single-player slice of Torment of Hailfire ({@link TormentOfHailfireEffect}),
 * reusing the same penalty-choice plumbing ({@code ChoiceContext.TormentPenaltyChoice} /
 * {@code PermanentChoiceContext.TormentSacrifice}). Not a spell target — the acted-on player is
 * derived from the stack entry, so {@code targetSpec()} stays {@code NONE}.
 *
 * @param lifeLoss  life lost when the player neither sacrifices a nonland permanent nor discards a card
 * @param recipient how the affected player is derived from the stack entry
 */
public record LoseLifeUnlessSacrificeNonlandOrDiscardEffect(int lifeLoss, LoseLifeRecipient recipient)
        implements CardEffect {

    /** Acts on the player carried on the stack entry's {@code targetId} (Torment of Scarabs). */
    public LoseLifeUnlessSacrificeNonlandOrDiscardEffect(int lifeLoss) {
        this(lifeLoss, LoseLifeRecipient.TARGET_PLAYER);
    }
}
