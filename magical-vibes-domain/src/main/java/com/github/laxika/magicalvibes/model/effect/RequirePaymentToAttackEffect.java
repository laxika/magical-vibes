package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.function.UnaryOperator;

/**
 * Defender-side attack tax: every creature attacking this permanent's controller costs
 * {@code amountPerAttacker} generic mana more to declare (CR 508.1h). The defender is the player the
 * creature attacks, or the controller of the planeswalker it attacks; a battle is never taxed. The
 * amount is evaluated from the defending player's perspective, so a board-derived
 * {@link DynamicAmount} (Sphere of Safety — enchantments you control) scales with the defender's
 * battlefield, and a target-relative amount reads the attacking creature:
 * {@code CountersOnTargetPermanent(null)} is "where X is the number of counters on that creature"
 * (Nils, Discipline Enforcer), nothing when the tax is asked without an attacker.
 *
 * <p>{@code activeCondition} (nullable, {@code null} = always active) gates the
 * whole tax on a state of the source permanent, re-checked at declare-attackers
 * time — Archangel of Tithes taxes only "as long as this creature is untapped".
 * {@code planeswalkersOnly} limits the tax to creatures attacking a
 * planeswalker controlled by the defender, as with Onakke Oathkeeper.
 *
 * <p>A resolution floats it on players with {@link GrantStaticEffectToPlayerEffect}, each player it
 * floats on being a defender: "until your next turn, creatures can't attack you" on {@code you()}
 * (Forbidding Spirit, Summon: Yojimbo), "this turn, creatures can't attack" on {@code eachPlayer()}
 * (War Tax). Its amount is fixed as the float begins (CR 608.2h); a floated copy has no source, so it
 * carries no {@code activeCondition}.
 *
 * <p>{@code includesBattles} ({@link #includingAttacksOnBattles()}) is "creatures can't attack unless …" with no
 * player named (War Tax): an attack on a battle is taxed too, judged for the battle's protector (CR 506.2).
 */
public record RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker,
                                           boolean protectsPlaneswalkers,
                                           boolean planeswalkersOnly,
                                           Condition activeCondition,
                                           boolean includesBattles) implements AmountsFixedWhenFloatedEffect {

    public RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker, boolean protectsPlaneswalkers,
                                        boolean planeswalkersOnly, Condition activeCondition) {
        this(amountPerAttacker, protectsPlaneswalkers, planeswalkersOnly, activeCondition, false);
    }

    public RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker) {
        this(amountPerAttacker, true, false, null);
    }

    public RequirePaymentToAttackEffect(int amountPerAttacker) {
        this(new Fixed(amountPerAttacker));
    }

    public RequirePaymentToAttackEffect(int amountPerAttacker, boolean protectsPlaneswalkers) {
        this(new Fixed(amountPerAttacker), protectsPlaneswalkers, false, null);
    }

    public RequirePaymentToAttackEffect(int amountPerAttacker, Condition activeCondition) {
        this(new Fixed(amountPerAttacker), true, false, activeCondition);
    }

    public RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker,
                                        boolean protectsPlaneswalkers,
                                        Condition activeCondition) {
        this(amountPerAttacker, protectsPlaneswalkers, false, activeCondition);
    }

    public static RequirePaymentToAttackEffect playerOnly(int amountPerAttacker) {
        return new RequirePaymentToAttackEffect(new Fixed(amountPerAttacker), false, false, null);
    }

    public static RequirePaymentToAttackEffect planeswalkersOnly(int amountPerAttacker) {
        return new RequirePaymentToAttackEffect(new Fixed(amountPerAttacker), true, true, null);
    }

    /** This tax also on attacks on battles: "creatures can't attack unless their controller pays" (War Tax). */
    public RequirePaymentToAttackEffect includingAttacksOnBattles() {
        return new RequirePaymentToAttackEffect(amountPerAttacker, protectsPlaneswalkers, planeswalkersOnly,
                activeCondition, true);
    }

    @Override
    public RequirePaymentToAttackEffect withAmountsFixed(UnaryOperator<DynamicAmount> fix) {
        return new RequirePaymentToAttackEffect(fix.apply(amountPerAttacker), protectsPlaneswalkers,
                planeswalkersOnly, activeCondition, includesBattles);
    }
}
