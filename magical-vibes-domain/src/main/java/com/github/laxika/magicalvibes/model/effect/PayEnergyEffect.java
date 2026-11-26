package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.Objects;

/**
 * "Pay {E}{E}" as an instruction, not a choice: the controller pays that many energy counters if they have them, and
 * can't pay more than they have or pay a part (CR 107.14, 118.3; Greenbelt Rampager's rulings: "If you have {E}{E}, you
 * have to pay it", "If you have only one {E}, you can't pay it"). Records {@code StepKind.PAY_ENERGY}: the energy paid
 * and the controller, or {@code StepOutcome.NONE} when they couldn't pay, so "If you can't, …" is
 * {@code ConditionalEffect(NotCondition(new HappenedThisWay(StepKind.PAY_ENERGY)), …)} after it (CR 118.12). A
 * "you may pay {E}" is {@code UnlessPaysEffect} with a {@link PayEnergyCost} ransom.
 *
 * @param amount how many energy counters, read as it resolves
 */
public record PayEnergyEffect(DynamicAmount amount) implements CardEffect {

    public PayEnergyEffect {
        Objects.requireNonNull(amount, "amount");
    }

    public PayEnergyEffect(int amount) {
        this(new Fixed(amount));
    }
}
