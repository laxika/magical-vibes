package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * {@code player} gains {@code amount} life. The player is {@link Subjects#you()} unless a constructor names another:
 * "target player gains 3 life" is {@code Subjects.targetPlayer()}, "its controller gains life equal to its toughness"
 * (Condemn) {@code Subjects.controllerOf(Subjects.target())}, "each opponent gains 10 life" (Aria of Flame)
 * {@code Subjects.eachOpponent()}. Several players gain in APNAP order.
 *
 * <p>The amount is evaluated once against the stack entry, except for a target player: then it is evaluated for each
 * target player with that player as both the amount's controller and its target ("target player gains X life, where X
 * is the greatest power among creatures they control").
 *
 * <p>Only {@link Subjects#targetPlayer()} declares a target (benign, any player). {@code targetsPlayer} also declares
 * one for an amount that reads a target player while you gain the life ("gain 2 life for each Mountain target
 * opponent controls", Renewing Dawn; Keeper of the Light).
 */
public record GainLifeEffect(DynamicAmount amount, PlayerSubject player,
                             boolean targetsPlayer) implements LifeGainEffect {

    public GainLifeEffect {
        Objects.requireNonNull(player, "player");
    }

    /** {@code player} gains {@code amount} life. */
    public GainLifeEffect(DynamicAmount amount, PlayerSubject player) {
        this(amount, player, false);
    }

    /** {@code player} gains {@code amount} life. */
    public GainLifeEffect(int amount, PlayerSubject player) {
        this(new Fixed(amount), player);
    }

    /** You gain {@code amount} life. */
    public GainLifeEffect(DynamicAmount amount) {
        this(amount, Subjects.you(), false);
    }

    /** You gain {@code amount} life. */
    public GainLifeEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return amount;
    }

    @Override
    public PlayerSubject lifeGainingPlayer() {
        return player;
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() || targetsPlayer
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
