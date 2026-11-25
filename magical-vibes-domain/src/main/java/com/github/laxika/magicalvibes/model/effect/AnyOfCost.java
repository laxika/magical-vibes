package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * A cost paid by doing one of several things ("Ward—Discard a card or pay {2}"): the payer chooses one alternative and
 * pays it (CR 118.12a). An alternative they couldn't pay in full can't be chosen (CR 118.3, 608.2d), so the cost can be
 * paid while any alternative can. Asked as a resolution-time ransom, each alternative the payer can pay is one yes or
 * no question, in the order the text lists them; declining every one is not paying.
 *
 * @param alternatives the ways to pay, in Oracle order, at least two
 */
public record AnyOfCost(List<CostEffect> alternatives) implements CostEffect {

    public AnyOfCost {
        Objects.requireNonNull(alternatives, "alternatives");
        alternatives = List.copyOf(alternatives);
        if (alternatives.size() < 2) {
            throw new IllegalArgumentException("An any-of cost has at least two alternatives: " + alternatives);
        }
        if (alternatives.stream().anyMatch(AnyOfCost.class::isInstance)) {
            throw new IllegalArgumentException("Alternatives don't nest: " + alternatives);
        }
    }

    public static AnyOfCost of(CostEffect first, CostEffect second) {
        return new AnyOfCost(List.of(first, second));
    }
}
