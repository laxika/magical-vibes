package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Static effect: "this creature can't attack [or block] unless its controller pays {@code amount}", an additional
 * generic mana cost locked in as attackers or blockers are declared (CR 508.1h, 509.1d). {@code amount} is read from
 * the creature as the declaration is made ({@code CountersOnSource(PLUS_ONE_PLUS_ONE)}: "{1} for each +1/+1 counter on
 * it", Myr Prototype, Phyrexian Marauder; a Cleric count for Whipgrass Entangler's grant).
 *
 * <p>{@code side} says which declarations cost it. A block cost with no {@code attackerFilter} is paid once for the
 * creature however many attackers it blocks ({@code GameQueryService.getPerCreatureBlockTax}); with one ("can't block
 * creatures with power 3 or greater unless you pay {1}", Hipparion) it is paid for each matching attacker it blocks.
 *
 * @param amount         the generic mana to pay, evaluated with the creature as the source
 * @param side           whether attacking, blocking or both cost it
 * @param attackerFilter for a block cost, the attackers whose blocking costs it; {@code null} for every attacker
 */
public record CantAttackOrBlockUnlessPaysEffect(DynamicAmount amount, Side side, PermanentPredicate attackerFilter)
        implements AttackCostEffect, BlockCostEffect {

    /** Which declarations the cost applies to. */
    public enum Side {
        ATTACK, BLOCK, ATTACK_OR_BLOCK;

        public boolean attacks() {
            return this != BLOCK;
        }

        public boolean blocks() {
            return this != ATTACK;
        }
    }

    public CantAttackOrBlockUnlessPaysEffect {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(side, "side");
        if (attackerFilter != null && side != Side.BLOCK) {
            throw new IllegalArgumentException("Only a block cost names the attackers it applies to");
        }
    }

    /** "This creature can't attack or block unless you pay {@code amount}." */
    public CantAttackOrBlockUnlessPaysEffect(DynamicAmount amount) {
        this(amount, Side.ATTACK_OR_BLOCK, null);
    }

    public CantAttackOrBlockUnlessPaysEffect(int amount) {
        this(new Fixed(amount));
    }

    /** "This creature can't attack unless you pay {@code amount}." */
    public static CantAttackOrBlockUnlessPaysEffect attacking(DynamicAmount amount) {
        return new CantAttackOrBlockUnlessPaysEffect(amount, Side.ATTACK, null);
    }

    /** "This creature can't block [attackers matching {@code attackerFilter}] unless you pay {@code amount}." */
    public static CantAttackOrBlockUnlessPaysEffect blocking(DynamicAmount amount, PermanentPredicate attackerFilter) {
        return new CantAttackOrBlockUnlessPaysEffect(amount, Side.BLOCK, attackerFilter);
    }

    @Override
    public int attackCost(Permanent creature) {
        return side.attacks() ? fixedAmount() : 0;
    }

    @Override
    public int blockCost(Permanent blocker, int attackerPower) {
        return side.blocks() ? fixedAmount() : 0;
    }

    @Override
    public DynamicAmount dynamicAttackCost() {
        return side.attacks() ? amount : null;
    }

    @Override
    public DynamicAmount dynamicBlockCost() {
        return side.blocks() ? amount : null;
    }

    private int fixedAmount() {
        return amount instanceof Fixed fixed ? fixed.value() : 0;
    }
}
