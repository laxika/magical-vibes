package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Grants protection from a specified card type until end of turn.
 */
public record GrantProtectionFromCardTypeUntilEndOfTurnEffect(
        CardType cardType,
        PermanentPredicate predicate,
        TargetPredicate declaredTarget
) implements CardEffect {

    public GrantProtectionFromCardTypeUntilEndOfTurnEffect(CardType cardType) {
        this(cardType, null, TargetPredicates.creature());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(declaredTarget, predicate);
    }
}
