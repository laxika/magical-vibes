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
 * @param repeatedGraveyardCardCount when positive, each payment also exiles this many cards
 *                                   from the controller's graveyard; the payment option should
 *                                   use a zero-mana symbol such as {@code {0}}
 * @param repeatedHandCardCount when positive, each payment also discards this many cards from
 *                              the controller's hand
 */
public record RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker,
                                            int maxPaymentsPerCost,
                                            List<PaymentOption> paymentOptions,
                                            int repeatedGraveyardCardCount,
                                            int repeatedHandCardCount) implements CostEffect {

    public record PaymentOption(String manaCost, boolean multikicker, int maxPayments) {
    }

    public RepeatableAdditionalManaCost {
        manaCosts = List.copyOf(manaCosts);
        paymentOptions = paymentOptions == null || paymentOptions.isEmpty()
                ? manaCosts.stream()
                        .map(manaCost -> new PaymentOption(manaCost, multikicker, maxPaymentsPerCost))
                        .toList()
                : List.copyOf(paymentOptions);
        if (repeatedGraveyardCardCount < 0) {
            throw new IllegalArgumentException("repeatedGraveyardCardCount cannot be negative");
        }
        if (repeatedHandCardCount < 0) {
            throw new IllegalArgumentException("repeatedHandCardCount cannot be negative");
        }
    }

    public RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker,
                                        int maxPaymentsPerCost, List<PaymentOption> paymentOptions,
                                        int repeatedGraveyardCardCount) {
        this(manaCosts, multikicker, maxPaymentsPerCost, paymentOptions,
                repeatedGraveyardCardCount, 0);
    }

    public RepeatableAdditionalManaCost(List<String> manaCosts) {
        this(manaCosts, false, Integer.MAX_VALUE, null, 0);
    }

    public RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker) {
        this(manaCosts, multikicker, Integer.MAX_VALUE, null, 0);
    }

    public RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker,
                                        int maxPaymentsPerCost) {
        this(manaCosts, multikicker, maxPaymentsPerCost, null, 0);
    }

    public RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker,
                                        int maxPaymentsPerCost, List<PaymentOption> paymentOptions) {
        this(manaCosts, multikicker, maxPaymentsPerCost, paymentOptions, 0);
    }

    public static RepeatableAdditionalManaCost multikicker(List<String> manaCosts) {
        return new RepeatableAdditionalManaCost(manaCosts, true, Integer.MAX_VALUE);
    }

    /** Creates a repeatable zero-mana cost that exiles N cards from the caster's graveyard per payment. */
    public static RepeatableAdditionalManaCost graveyardExile(int cardCount) {
        if (cardCount < 1) {
            throw new IllegalArgumentException("cardCount must be positive");
        }
        return new RepeatableAdditionalManaCost(List.of("{0}"), false, Integer.MAX_VALUE, null, cardCount, 0);
    }

    /** Creates a repeatable additional mana cost that also discards one card per payment. */
    public static RepeatableAdditionalManaCost withDiscard(List<String> manaCosts) {
        return new RepeatableAdditionalManaCost(manaCosts, false, Integer.MAX_VALUE, null, 0, 1);
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
        int repeatedHandCardCount = costs.stream()
                .mapToInt(RepeatableAdditionalManaCost::repeatedHandCardCount)
                .max()
                .orElse(0);
        return new RepeatableAdditionalManaCost(
                manaCosts, multikicker, maxPaymentsPerCost, options, 0, repeatedHandCardCount);
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
