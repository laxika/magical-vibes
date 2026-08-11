package com.github.laxika.magicalvibes.model.effect;

/**
 * The targeted player may exile a card from their graveyard. If they do not, the ability's
 * controller gets a separate option to draw a card.
 */
public record TargetPlayerMayExileCardFromGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
