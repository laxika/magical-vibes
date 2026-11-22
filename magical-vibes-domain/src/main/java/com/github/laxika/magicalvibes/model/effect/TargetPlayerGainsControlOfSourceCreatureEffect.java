package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives {@code player} control of {@code permanent}, the source permanent unless another is named, for
 * {@code duration}. The normal form targets that player; {@link #triggeringPlayer()} uses the non-targeting player
 * the surrounding trigger baked into the stack entry's target. The overload with a follow-up effect is used for
 * combat-damage abilities whose follow-up happens only after control changes successfully.
 *
 * @param player     who gains control: {@link Subjects#targetPlayer()}, the trigger's player
 *                   ({@link Subjects#targetPlayerDeclaredElsewhere()}), {@link Subjects#you()} (the player who
 *                   activated the ability, Oft-Nabbed Goat) or {@code controllerOf(target())} ("that permanent's
 *                   controller", Starke of Rath)
 * @param thenEffect what follows a control change, or {@code null}
 * @param duration   how long the control change lasts
 * @param permanent  whose control changes: {@link Subjects#source()} or {@link Subjects#attached()} (Assault Suit)
 */
public record TargetPlayerGainsControlOfSourceCreatureEffect(PlayerSubject player, CardEffect thenEffect,
                                                              Duration duration, PermanentSubject permanent)
        implements ControlStealingEffect {

    public TargetPlayerGainsControlOfSourceCreatureEffect {
        if (!(player instanceof PlayerSubject.You || player instanceof PlayerSubject.TargetPlayer
                || player instanceof PlayerSubject.ControllerOf || player instanceof PlayerSubject.PlayersThisWay)) {
            throw new IllegalArgumentException("One player gains control: " + player);
        }
        if (!permanent.equals(Subjects.source()) && !permanent.equals(Subjects.attached())
                && !(permanent.equals(Subjects.target()) && !player.declaresTarget())) {
            throw new IllegalArgumentException("Unsupported permanent to gain control of: " + permanent);
        }
    }

    /** {@code player} gains control of {@code permanent} for {@code duration}. */
    public TargetPlayerGainsControlOfSourceCreatureEffect(PlayerSubject player, Duration duration,
                                                          PermanentSubject permanent) {
        this(player, null, duration, permanent);
    }

    /** {@code player} gains control of the source with no stated duration. */
    public TargetPlayerGainsControlOfSourceCreatureEffect(PlayerSubject player) {
        this(player, Duration.PERMANENT, Subjects.source());
    }

    public TargetPlayerGainsControlOfSourceCreatureEffect(boolean targetsPlayer, CardEffect thenEffect,
                                                          Duration duration) {
        this(targetsPlayer ? Subjects.targetPlayer() : Subjects.targetPlayerDeclaredElsewhere(), thenEffect,
                duration, Subjects.source());
    }

    public TargetPlayerGainsControlOfSourceCreatureEffect() {
        this(true, null, Duration.PERMANENT);
    }

    public TargetPlayerGainsControlOfSourceCreatureEffect(boolean targetsPlayer) {
        this(targetsPlayer, null, Duration.PERMANENT);
    }

    public TargetPlayerGainsControlOfSourceCreatureEffect(boolean targetsPlayer, CardEffect thenEffect) {
        this(targetsPlayer, thenEffect, Duration.PERMANENT);
    }

    public static TargetPlayerGainsControlOfSourceCreatureEffect triggeringPlayer() {
        return triggeringPlayer(Duration.PERMANENT);
    }

    public static TargetPlayerGainsControlOfSourceCreatureEffect triggeringPlayer(CardEffect thenEffect) {
        return triggeringPlayer(Duration.PERMANENT, thenEffect);
    }

    public static TargetPlayerGainsControlOfSourceCreatureEffect triggeringPlayer(Duration duration) {
        return new TargetPlayerGainsControlOfSourceCreatureEffect(false, null, duration);
    }

    public static TargetPlayerGainsControlOfSourceCreatureEffect triggeringPlayer(
            Duration duration, CardEffect thenEffect) {
        return new TargetPlayerGainsControlOfSourceCreatureEffect(false, thenEffect, duration);
    }

    @Override
    public TargetSpec targetSpec() {
        if (player.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return permanent.declaresTarget() ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }


    @Override
    public Duration controlDuration() {
        return duration;
    }
}
