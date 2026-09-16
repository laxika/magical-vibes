package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the target permanent until a player other than the ability controller becomes the
 * monarch. The controller is captured when the ability resolves.
 */
public record ExileTargetPermanentUntilOpponentBecomesMonarchEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
