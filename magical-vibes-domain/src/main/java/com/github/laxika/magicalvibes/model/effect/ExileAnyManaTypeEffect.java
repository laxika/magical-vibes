package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may spend mana as though it were mana of any type to cast that spell": a floating effect on an exiled card
 * ({@code FloatingContinuousEffect.onCard}) that lasts as long as its card states and ends as the card leaves exile
 * (CR 400.7). It applies to the card whichever permission lets it be cast; an any-type rider that belongs to a play
 * permission is {@link ExilePlayPermissionEffect#anyManaType()} instead.
 */
public record ExileAnyManaTypeEffect() implements CardEffect {
}
