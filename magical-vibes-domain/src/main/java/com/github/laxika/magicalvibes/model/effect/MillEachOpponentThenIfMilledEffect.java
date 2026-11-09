package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills each opponent and resolves the follow-up if at least one card was milled this way. It leaves the stack
 * entry's event value alone, so a follow-up reads the triggering spell's mana value from it (Saruman of Many Colors).
 */
public record MillEachOpponentThenIfMilledEffect(int count, CardEffect thenEffect)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return thenEffect.targetSpec();
    }
}
