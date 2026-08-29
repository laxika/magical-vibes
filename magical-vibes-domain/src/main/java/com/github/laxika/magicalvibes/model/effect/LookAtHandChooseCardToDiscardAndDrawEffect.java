package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at a target player's hand. The controller may choose a card from it; if they do, that
 * player discards the chosen card and draws a card.
 */
public record LookAtHandChooseCardToDiscardAndDrawEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
