package com.github.laxika.magicalvibes.model.effect;

/**
 * Word of Command's resolution effect: the controller chooses a card from the target opponent's
 * hand, then controls that player while they play it if able.
 */
public record WordOfCommandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
