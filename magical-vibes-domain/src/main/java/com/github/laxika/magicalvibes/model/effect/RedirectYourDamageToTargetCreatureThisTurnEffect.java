package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * "All damage that would be dealt [this turn] to [you / you and permanents you control / you and creatures you control /
 * creatures you control] is dealt to [destination] instead" (CR 614.9). As it resolves, the effect's controller is the
 * protected player, {@code destination} names the one permanent that is dealt the damage instead, and a redirection
 * lasting {@code duration} begins. The redirection does nothing while that permanent is off the battlefield or is no
 * longer a creature (CR 614.9; with {@code destinationMayBePlaneswalker}, a creature, planeswalker or battle), and
 * damage dealt to it is dealt normally. Nothing begins when {@code destination} names no permanent as it resolves.
 *
 * <p>Cards: Heroic Sacrifice (target creature you control; you and creatures you control), Saving Grace
 * ({@code attached()}; you and permanents you control), Gideon's Sacrifice (the chosen creature or planeswalker,
 * {@code thisWay(CHOOSE)}), Ascent of the Worthy (the chosen creature; creatures you control, until your next turn),
 * Turn the Tables (target attacking creature; combat damage to you), Shimian Night Stalker ({@code source()}; damage to
 * you dealt by target attacking creature).</p>
 *
 * @param destination   the permanent dealt the damage instead: {@code target()}, {@code source()}, {@code attached()} or a
 *                      step result such as {@code thisWay(CHOOSE)}
 * @param redirected    whose damage is redirected
 * @param damageSource  the one source whose damage is redirected, found as the effect resolves; {@code null} for any source
 * @param combatOnly    whether only combat damage is redirected
 * @param duration      how long the redirection lasts
 * @param targetFilter  what the target must also be, when {@code destination} or {@code damageSource} is the target;
 *                      {@code null} for any creature
 * @param destinationMayBePlaneswalker whether the destination still receives the damage while it is a planeswalker or
 *                      battle rather than a creature ("the chosen permanent" of Gideon's Sacrifice); otherwise it must
 *                      still be a creature (Saving Grace's ruling)
 */
public record RedirectYourDamageToTargetCreatureThisTurnEffect(PermanentSubject destination,
                                                               RedirectedDamage redirected,
                                                               PermanentSubject damageSource,
                                                               boolean combatOnly,
                                                               Duration duration,
                                                               PermanentPredicate targetFilter,
                                                               boolean destinationMayBePlaneswalker) implements CardEffect {

    /** Whose damage the redirection covers. */
    public enum RedirectedDamage {
        /** Damage to the protected player only. */
        YOU(true, false, false),
        /** Damage to the protected player and to creatures they control. */
        YOU_AND_CREATURES(true, true, true),
        /** Damage to the protected player and to permanents they control. */
        YOU_AND_PERMANENTS(true, true, false),
        /** Damage to creatures the protected player controls, not to the player. */
        CREATURES(false, true, true);

        private final boolean player;
        private final boolean permanents;
        private final boolean creaturesOnly;

        RedirectedDamage(boolean player, boolean permanents, boolean creaturesOnly) {
            this.player = player;
            this.permanents = permanents;
            this.creaturesOnly = creaturesOnly;
        }

        /** Whether damage to the protected player is covered. */
        public boolean coversPlayer() {
            return player;
        }

        /** Whether damage to permanents the protected player controls is covered (only creatures if {@link #creaturesOnly()}). */
        public boolean coversPermanents() {
            return permanents;
        }

        /** Whether only creatures among those permanents are covered. */
        public boolean creaturesOnly() {
            return creaturesOnly;
        }
    }

    public RedirectYourDamageToTargetCreatureThisTurnEffect {
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(redirected, "redirected");
        Objects.requireNonNull(duration, "duration");
        if (destination.declaresTarget() && damageSource != null && damageSource.declaresTarget()) {
            throw new IllegalArgumentException("Only one of destination and damage source may be the target");
        }
        if (targetFilter != null && !destination.declaresTarget()
                && (damageSource == null || !damageSource.declaresTarget())) {
            throw new IllegalArgumentException("A target filter needs a target subject");
        }
    }

    /** Heroic Sacrifice: "all damage that would be dealt to you and creatures you control is dealt to the chosen creature". */
    public RedirectYourDamageToTargetCreatureThisTurnEffect() {
        this(Subjects.target(), RedirectedDamage.YOU_AND_CREATURES, null, false, Duration.UNTIL_END_OF_TURN,
                new PermanentControlledBySourceControllerPredicate(), false);
    }

    /** "All damage that would be dealt this turn to [redirected] is dealt to [destination] instead." */
    public static RedirectYourDamageToTargetCreatureThisTurnEffect to(PermanentSubject destination,
                                                                      RedirectedDamage redirected) {
        return new RedirectYourDamageToTargetCreatureThisTurnEffect(destination, redirected, null, false,
                Duration.UNTIL_END_OF_TURN, null, false);
    }

    /** As this, lasting {@code duration} ("until your next turn"). */
    public RedirectYourDamageToTargetCreatureThisTurnEffect lasting(Duration duration) {
        return new RedirectYourDamageToTargetCreatureThisTurnEffect(destination, redirected, damageSource, combatOnly,
                duration, targetFilter, destinationMayBePlaneswalker);
    }

    /** As this, for combat damage only ("all combat damage that would be dealt to you"). */
    public RedirectYourDamageToTargetCreatureThisTurnEffect combatDamageOnly() {
        return new RedirectYourDamageToTargetCreatureThisTurnEffect(destination, redirected, damageSource, true,
                duration, targetFilter, destinationMayBePlaneswalker);
    }

    /** As this, for the damage {@code source} deals only ("all damage that would be dealt to you by target creature"). */
    public RedirectYourDamageToTargetCreatureThisTurnEffect dealtBy(PermanentSubject source) {
        return new RedirectYourDamageToTargetCreatureThisTurnEffect(destination, redirected, source, combatOnly,
                duration, targetFilter, destinationMayBePlaneswalker);
    }

    /** As this, with the target narrowed by {@code filter} ("target attacking creature"). */
    public RedirectYourDamageToTargetCreatureThisTurnEffect targeting(PermanentPredicate filter) {
        return new RedirectYourDamageToTargetCreatureThisTurnEffect(destination, redirected, damageSource, combatOnly,
                duration, filter, destinationMayBePlaneswalker);
    }

    /** As this, still dealing the damage to the destination while it is a planeswalker or battle ("the chosen permanent"). */
    public RedirectYourDamageToTargetCreatureThisTurnEffect orPlaneswalkerDestination() {
        return new RedirectYourDamageToTargetCreatureThisTurnEffect(destination, redirected, damageSource, combatOnly,
                duration, targetFilter, true);
    }

    /**
     * A target destination is harmful when only damage to you is redirected: it is the creature that ends up taking
     * the damage, an attacker (Turn the Tables). A destination that also takes your permanents' damage is one of your
     * own (Heroic Sacrifice), and a target damage source is only named (Shimian Night Stalker): both benign.
     */
    @Override
    public TargetSpec targetSpec() {
        if (destination.declaresTarget()) {
            return redirectsOnlyYourDamage()
                    ? TargetSpec.harmful(TargetPredicates.creature(), targetFilter)
                    : TargetSpec.benign(TargetPredicates.creature(), targetFilter);
        }
        if (damageSource != null && damageSource.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.creature(), targetFilter);
        }
        return TargetSpec.NONE;
    }

    /** Whether only damage to the protected player is redirected, none to their permanents. */
    public boolean redirectsOnlyYourDamage() {
        return !redirected.coversPermanents();
    }
}
