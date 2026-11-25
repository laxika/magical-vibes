package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.Objects;

/**
 * A cost paid by doing several things ("pay {1} and discard a card", "sacrifice this enchantment and pay {2}{G}{G}"):
 * all of them or none (CR 601.2h: "Partial payments are not allowed"; CR 118.3). Asked as a resolution-time payment, the
 * payer is asked once for the whole cost; paying checks that every part can be paid, then pays the mana first and the
 * other parts in Oracle order, a part that asks (the card to discard, the permanent to sacrifice) finishing before the
 * next is paid.
 *
 * @param parts what is paid, in Oracle order: at least two, none of them an {@link AllOfCost} or {@link AnyOfCost}, and
 *              at most one {@link PayManaCost}
 */
public record AllOfCost(List<CostEffect> parts) implements CostEffect {

    public AllOfCost {
        Objects.requireNonNull(parts, "parts");
        parts = List.copyOf(parts);
        if (parts.size() < 2) {
            throw new IllegalArgumentException("An all-of cost has at least two parts: " + parts);
        }
        if (parts.stream().anyMatch(part -> part instanceof AllOfCost || part instanceof AnyOfCost)) {
            throw new IllegalArgumentException("Parts don't nest: " + parts);
        }
        if (parts.stream().filter(PayManaCost.class::isInstance).count() > 1) {
            throw new IllegalArgumentException("An all-of cost has at most one mana part: " + parts);
        }
    }

    public static AllOfCost of(CostEffect... parts) {
        return new AllOfCost(List.of(parts));
    }

    /** The mana part, or {@code null}. */
    public PayManaCost manaPart() {
        return parts.stream().filter(PayManaCost.class::isInstance).map(PayManaCost.class::cast).findFirst()
                .orElse(null);
    }
}
