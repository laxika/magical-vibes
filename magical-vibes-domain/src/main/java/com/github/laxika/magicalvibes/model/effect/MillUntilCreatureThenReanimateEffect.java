package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player mills a card, repeating until a creature card is put into their graveyard this way
 * or the chosen X cards have been milled, whichever comes first. If a creature card was put into
 * that graveyard this way, the source permanent is sacrificed and that creature is put onto the
 * battlefield under the ability controller's control.
 * <p>
 * Used by Helm of Obedience. The card restricts the target to an opponent; X comes from the
 * activation's paid X (X can't be 0, so nothing happens at X = 0).
 */
public record MillUntilCreatureThenReanimateEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
