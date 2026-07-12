package com.github.laxika.magicalvibes.model.effect;

/**
 * Target permanent becomes a color the controller chooses until end of turn (CR 105.3 / layer 5).
 * On resolution the controller is prompted for a color ({@code ChoiceContext.ColorSetChoice}); the
 * choice handler then applies the same floating layer-5 color-setting effect as
 * {@link GrantColorUntilEndOfTurnEffect} (which replaces all previous colors). Used by Distorting Lens.
 */
public record SetChosenColorUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
