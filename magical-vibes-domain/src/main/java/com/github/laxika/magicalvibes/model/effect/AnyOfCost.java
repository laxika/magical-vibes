package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;

/**
 * A cost paid by doing one of several things ("Ward—Discard a card or pay {2}"): the payer chooses one alternative and
 * pays it (CR 118.12a). An alternative they couldn't pay in full can't be chosen (CR 118.3, 608.2d), so the cost can be
 * paid while any alternative can. Asked as a resolution-time ransom, each alternative the payer can pay is one yes or
 * no question, in the order the text lists them; declining every one is not paying.
 *
 * <p>As an additional cost to cast a spell ("As an additional cost to cast this spell, [A] or [B]", CR 118.8,
 * {@code Card.addCastingCost}), the caster announces which alternative they pay (CR 601.2b), that alternative is part of
 * the total cost (CR 601.2f) and it is paid like the same cost standing alone (CR 601.2h):
 * {@code AdditionalSpellCostService.chosenAlternative} reads the choice from the cast request. A free cast offers the
 * payable alternatives in this record's order.
 * The AI's cost facets below read the first alternative that has one.
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

    public static AnyOfCost of(CostEffect first, CostEffect second, CostEffect third) {
        return new AnyOfCost(List.of(first, second, third));
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return alternatives.stream().map(CostEffect::consumedPermanentFilter).filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    @Override
    public boolean sacrificesChosenCreature() {
        return alternatives.stream().anyMatch(CostEffect::sacrificesChosenCreature);
    }

    @Override
    public DynamicAmount tappedPermanentCount() {
        return alternatives.stream().map(CostEffect::tappedPermanentCount).filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    @Override
    public int consumedGraveyardCardCount() {
        return alternatives.stream().mapToInt(CostEffect::consumedGraveyardCardCount).filter(count -> count > 0)
                .findFirst().orElse(0);
    }

    @Override
    public CardType consumedGraveyardCardType() {
        return alternatives.stream().filter(alternative -> alternative.consumedGraveyardCardCount() > 0)
                .map(CostEffect::consumedGraveyardCardType).findFirst().orElse(null);
    }
}
