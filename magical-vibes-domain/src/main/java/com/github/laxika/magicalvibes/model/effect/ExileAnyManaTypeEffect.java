package com.github.laxika.magicalvibes.model.effect;

/**
 * "Mana of any type can be spent to cast that spell" (or, with {@code asThoughAnyColor}, "you may spend mana as though
 * it were mana of any color to cast that spell", which leaves a {C} symbol to colorless mana, CR 107.4c): a floating
 * effect on an exiled card ({@code FloatingContinuousEffect.onCard}) that lasts as long as its card states and ends as
 * the card leaves exile (CR 400.7). It applies to the card whichever permission lets it be cast; an any-mana rider that
 * belongs to a play permission is {@link ExilePlayPermissionEffect#manaSpending()} instead.
 *
 * @param asThoughAnyColor the any-color wording rather than any type
 */
public record ExileAnyManaTypeEffect(boolean asThoughAnyColor) implements CardEffect {

    /** The any-type rider. */
    public ExileAnyManaTypeEffect() {
        this(false);
    }
}
