package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * {@code player} loses {@code lifeLoss} life unless they sacrifice a nonland permanent of their choice or discard a
 * card. The affected player picks one of the three outcomes; the offered options are pruned to what they can actually
 * do (losing life is always available), so a player who can neither sacrifice nor discard just loses life without a
 * prompt.
 *
 * <p>{@code player} is "that player" ({@link Subjects#triggeringPlayer()}, Torment of Scarabs), a target player
 * (Thornplate Intimidator, Tergrid's Lantern), or "its controller" of a permanent another effect targets
 * ({@code controllerOf(target())}, Torment of Venom); that permanent itself is then excluded from the sacrifice options
 * ("sacrifice <em>another</em> nonland permanent"). Pair the last with a sibling targeting effect that owns the
 * permanent target (e.g. {@link PutCounterOnTargetPermanentEffect}), listed first so the creature is still present.
 *
 * <p>The single-pass, single-player slice of Torment of Hailfire ({@link TormentOfHailfireEffect}), reusing the same
 * penalty-choice plumbing ({@code ChoiceContext.TormentPenaltyChoice} / {@code PermanentChoiceContext.TormentSacrifice}).
 * Only {@link Subjects#targetPlayer()} declares a target (benign, any player).
 *
 * @param lifeLoss life lost when the player neither sacrifices a nonland permanent nor discards a card
 * @param player   who chooses and loses life
 */
public record LoseLifeUnlessSacrificeNonlandOrDiscardEffect(int lifeLoss, PlayerSubject player)
        implements CardEffect {

    public LoseLifeUnlessSacrificeNonlandOrDiscardEffect {
        Objects.requireNonNull(player, "player");
    }

    /** "That player" (Torment of Scarabs). */
    public LoseLifeUnlessSacrificeNonlandOrDiscardEffect(int lifeLoss) {
        this(lifeLoss, Subjects.triggeringPlayer());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
