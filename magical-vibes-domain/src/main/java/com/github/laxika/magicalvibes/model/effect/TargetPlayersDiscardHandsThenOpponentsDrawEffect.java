package com.github.laxika.magicalvibes.model.effect;

/**
 * Each selected player discards their hand. Each selected opponent then draws one fewer card than
 * they discarded, while the controller draws seven if they discarded at least one card this way.
 */
public record TargetPlayersDiscardHandsThenOpponentsDrawEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
