package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Gains {@code amount} life for the activated ability's controller only when the permanent
 * sacrificed to pay the ability's cost matches {@code filter} ("If a Saproling was sacrificed this
 * way, you gain 2 life"). The sacrificed card is read from the stack entry's payment-time snapshot.
 */
public record GainLifeIfSacrificedCardMatchesEffect(CardPredicate filter, int amount) implements CardEffect {
}
