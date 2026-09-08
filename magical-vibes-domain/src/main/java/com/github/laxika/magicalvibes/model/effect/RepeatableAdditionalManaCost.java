package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * SPELL-slot additional cast cost: "As an additional cost to cast this spell, you may pay
 * [cost A] and/or [cost B] any number of times" (Primitive Justice). Each repetition is one
 * payment of one of {@code manaCosts}; the caster announces the chosen payments as the spell is
 * cast and the engine appends them to the spell's total mana cost, exactly like escalate.
 *
 * <p>The spell's announced X is the number of targets the payments buy — {@code 1 + repetitions} —
 * so pairing this with {@code Card.targetX} makes the target group scale with the payments made.
 * The individual chosen payments are snapshotted onto the stack entry so that a resolution-time
 * {@link com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount} can read how
 * many times a particular option was paid (Primitive Justice's "you gain 1 life for each
 * additional {1}{G} you paid").
 *
 * @param multikicker whether the repeated payments are multikicker payments and therefore count
 *                    as kicks for kicked-spell triggers
 * @param maxPaymentsPerCost maximum number of times each declared cost may be paid
 */
public record RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker,
                                            int maxPaymentsPerCost,
                                            List<PaymentOption> paymentOptions) implements CostEffect {

    public record PaymentOption(String manaCost, boolean multikicker, int maxPayments) {
    }

    public RepeatableAdditionalManaCost {
        manaCosts = List.copyOf(manaCosts);
        paymentOptions = paymentOptions == null || paymentOptions.isEmpty()
                ? manaCosts.stream()
                        .map(manaCost -> new PaymentOption(manaCost, multikicker, maxPaymentsPerCost))
                        .toList()
                : List.copyOf(paymentOptions);
    }

    public RepeatableAdditionalManaCost(List<String> manaCosts) {
        this(manaCosts, false, Integer.MAX_VALUE, null);
    }

    public RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker) {
        this(manaCosts, multikicker, Integer.MAX_VALUE, null);
    }

    public RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker,
                                        int maxPaymentsPerCost) {
        this(manaCosts, multikicker, maxPaymentsPerCost, null);
    }

    public static RepeatableAdditionalManaCost multikicker(List<String> manaCosts) {
        return new RepeatableAdditionalManaCost(manaCosts, true, Integer.MAX_VALUE);
    }

    /** Creates an optional additional cost that may be paid at most once. */
    public static RepeatableAdditionalManaCost singlePayment(List<String> manaCosts) {
        return new RepeatableAdditionalManaCost(manaCosts, false, 1);
    }

    public static RepeatableAdditionalManaCost combine(List<RepeatableAdditionalManaCost> costs) {
        List<PaymentOption> options = costs.stream()
                .flatMap(cost -> cost.paymentOptions().stream())
                .toList();
        List<String> manaCosts = options.stream()
                .map(PaymentOption::manaCost)
                .distinct()
                .toList();
        boolean multikicker = options.stream().anyMatch(PaymentOption::multikicker);
        int maxPaymentsPerCost = options.stream()
                .mapToInt(PaymentOption::maxPayments)
                .min()
                .orElse(Integer.MAX_VALUE);
        return new RepeatableAdditionalManaCost(manaCosts, multikicker, maxPaymentsPerCost, options);
    }

    /** Counts payments assigned to options that are multikicker payments. */
    public int multikickerPaymentCount(List<String> payments) {
        if (payments == null || payments.isEmpty()) {
            return 0;
        }
        int[] counts = new int[paymentOptions.size()];
        int multikickerPayments = 0;
        for (String payment : payments) {
            for (int i = 0; i < paymentOptions.size(); i++) {
                PaymentOption option = paymentOptions.get(i);
                if (option.manaCost().equals(payment) && counts[i] < option.maxPayments()) {
                    counts[i]++;
                    if (option.multikicker()) {
                        multikickerPayments++;
                    }
                    break;
                }
            }
        }
        return multikickerPayments;
    }
}
