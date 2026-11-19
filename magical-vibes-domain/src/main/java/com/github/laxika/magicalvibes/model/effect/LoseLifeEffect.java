package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * {@code player} loses {@code amount} life. One record covers the whole life-loss family: the {@link PlayerSubject}
 * names who loses life (you, target player, that player, each opponent, its controller, ...) and the
 * {@link DynamicAmount} covers fixed amounts ("loses 3 life"), an X value (Exsanguinate), or a relational amount such
 * as {@code PermanentCount} ("1 life for each Vampire you control", Bishop of the Bloodstained) or {@code EventValue}
 * ("equal to the life you gained", Sanguine Bond). When {@code controllerGainsLifeLost} is set the controller then gains
 * life equal to the total life lost this way (the "each opponent loses N life, you gain that much" drain pattern —
 * Blood Tithe, Chancellor of the Dross, Exsanguinate).
 *
 * <p>Life loss is <em>not</em> damage: it adjusts the life total (CR 119.3), it is applied through the shared life-loss primitive and feeds
 * "loses life" triggers, never through damage plumbing.
 *
 * <p>{@code exemptIfControls} narrows an "each player" / "each opponent" subject ({@link PlayerSubject.Each},
 * {@link PlayerSubject.OpponentsOf}) to the players who control no permanent matching that predicate — "each opponent
 * who doesn't control an Elf loses 1 life" (Thornbow Archer). The other subjects ignore it.
 *
 * <p>Only {@link Subjects#targetPlayer()} declares a target (benign, any player).
 *
 * @param amount                  amount of life to lose
 * @param player                  who loses life
 * @param controllerGainsLifeLost when {@code true} the controller gains the total life lost
 * @param exemptIfControls        when non-null, players controlling a matching permanent are skipped
 * @param controllerGainsLifeLostCondition optional condition gating the controller's life gain
 * @param recordTargetLifeLost record the actual life lost as the resolution event value
 */
public record LoseLifeEffect(DynamicAmount amount, PlayerSubject player,
                             boolean controllerGainsLifeLost,
                             PermanentPredicate exemptIfControls,
                             Condition controllerGainsLifeLostCondition,
                             boolean recordTargetLifeLost) implements CardEffect {

    public LoseLifeEffect {
        Objects.requireNonNull(player, "player");
    }

    public LoseLifeEffect(DynamicAmount amount, PlayerSubject player,
            boolean controllerGainsLifeLost, PermanentPredicate exemptIfControls,
            Condition controllerGainsLifeLostCondition) {
        this(amount, player, controllerGainsLifeLost, exemptIfControls,
                controllerGainsLifeLostCondition, false);
    }

    public LoseLifeEffect(DynamicAmount amount, PlayerSubject player,
            boolean controllerGainsLifeLost, PermanentPredicate exemptIfControls) {
        this(amount, player, controllerGainsLifeLost, exemptIfControls, null);
    }

    /** Dynamic amount, with drain flag, no exemption. */
    public LoseLifeEffect(DynamicAmount amount, PlayerSubject player, boolean controllerGainsLifeLost) {
        this(amount, player, controllerGainsLifeLost, null, null);
    }

    /** Fixed amount, with drain flag. */
    public LoseLifeEffect(int amount, PlayerSubject player, boolean controllerGainsLifeLost) {
        this(new Fixed(amount), player, controllerGainsLifeLost, null, null);
    }

    /** Dynamic amount, non-drain. */
    public LoseLifeEffect(DynamicAmount amount, PlayerSubject player) {
        this(amount, player, false, null, null);
    }

    /** Fixed amount, non-drain. */
    public LoseLifeEffect(int amount, PlayerSubject player) {
        this(new Fixed(amount), player, false, null, null);
    }

    /** Fixed amount, non-drain, skipping players who control a matching permanent. */
    public LoseLifeEffect(int amount, PlayerSubject player, PermanentPredicate exemptIfControls) {
        this(new Fixed(amount), player, false, exemptIfControls);
    }

    /** Fixed amount, controller loses life, non-drain (self life-loss cost/drawback). */
    public LoseLifeEffect(int amount) {
        this(new Fixed(amount), Subjects.you(), false, null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
