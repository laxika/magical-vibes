package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * One or more permanents (the dealers) each deal damage to players: "target creature deals damage equal to its power
 * to that creature's controller", "it deals 1 damage to each opponent", "each creature deals 1 damage to its
 * controller", "enchanted creature deals 1 damage to its owner". Each dealer is the source of its own damage
 * (CR 120.1): its colours, lifelink, deathtouch, infect and damage triggers apply, and its controller is the damage
 * source's controller. A dealer that left the battlefield deals the damage as it last existed there (CR 608.2h).
 *
 * <p>The dealers ({@code subject}): {@code target()} (declares a harmful creature target), the tokens an earlier step
 * created ({@code createdThisResolution()}), the enchanted or equipped creature ({@code attached()},
 * {@code attachedCreature()}), the permanent the trigger event was about ({@code triggering()}, or
 * {@code triggeringOrSource()} for "this creature or equipped creature"), or a set ({@code allCreatures()}). No other
 * subject may declare a target.
 *
 * <p>The players ({@code player}): {@code controllerOf(subject)} / {@code ownerOf(subject)} name each dealer's own
 * controller or owner, as it is or last was; any other non-targeting player subject is resolved once for the ability
 * ({@code you()}, {@code eachOpponent()}, {@code defendingPlayer()}, {@code except(eachOpponent(), triggeringPlayer())}).
 *
 * @param damage  the amount each dealer deals, evaluated with that dealer as the amount's source ({@code SourcePower}
 *                is its power, its last-known power if it left), fixed as the effect resolves (CR 608.2h)
 * @param subject the dealers
 * @param player  the players each dealer deals the damage to
 */
public record TargetCreatureDealsDamageToControllerEffect(DynamicAmount damage, PermanentSubject subject,
                                                           PlayerSubject player)
        implements DamageDealingEffect {

    public TargetCreatureDealsDamageToControllerEffect {
        if (subject.declaresTarget() && !(subject instanceof PermanentSubject.Target target && target.group() == null)) {
            throw new IllegalArgumentException("Unsupported damage source subject: " + subject);
        }
        if (player.declaresTarget()) {
            throw new IllegalArgumentException("The damaged players are not targeted: " + player);
        }
        if ((player instanceof PlayerSubject.ControllerOf controllerOf && !controllerOf.permanent().equals(subject))
                || (player instanceof PlayerSubject.OwnerOf ownerOf && !ownerOf.permanent().equals(subject))) {
            throw new IllegalArgumentException("\"Its controller\" / \"its owner\" must name the dealers: " + player);
        }
    }

    public TargetCreatureDealsDamageToControllerEffect(DynamicAmount damage) {
        this(damage, Subjects.target(), Subjects.controllerOf(Subjects.target()));
    }

    public TargetCreatureDealsDamageToControllerEffect(int damage) {
        this(new Fixed(damage));
    }

    /** "{@code dealers} each deal {@code damage} damage to {@code player}". */
    public static TargetCreatureDealsDamageToControllerEffect dealtBy(PermanentSubject dealers, int damage,
                                                                      PlayerSubject player) {
        return new TargetCreatureDealsDamageToControllerEffect(new Fixed(damage), dealers, player);
    }

    /** "{@code dealers} each deal {@code damage} to {@code player}". */
    public static TargetCreatureDealsDamageToControllerEffect dealtBy(PermanentSubject dealers, DynamicAmount damage,
                                                                      PlayerSubject player) {
        return new TargetCreatureDealsDamageToControllerEffect(damage, dealers, player);
    }

    /** Whether {@code player} names each dealer's own controller or owner rather than players found once. */
    public boolean playerIsEachDealers() {
        return player instanceof PlayerSubject.ControllerOf || player instanceof PlayerSubject.OwnerOf;
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.harmful(TargetPredicates.creature())
                : TargetSpec.NONE;
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public boolean damagesController() {
        return player instanceof PlayerSubject.You;
    }
}
