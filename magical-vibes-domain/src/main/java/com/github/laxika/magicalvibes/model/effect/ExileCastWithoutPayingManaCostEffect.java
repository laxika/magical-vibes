package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may cast that card without paying its mana cost": a floating effect on an exiled card
 * ({@code FloatingContinuousEffect.onCard}) for a card that is cast through something other than a play permission of
 * its own (a copy cast from exile); it ends as the card leaves exile (CR 400.7). When the card has an
 * {@link ExilePlayPermissionEffect}, the same rider is {@link ExilePlayPermissionEffect#withoutPayingManaCost()}.
 */
public record ExileCastWithoutPayingManaCostEffect() implements CardEffect {
}
