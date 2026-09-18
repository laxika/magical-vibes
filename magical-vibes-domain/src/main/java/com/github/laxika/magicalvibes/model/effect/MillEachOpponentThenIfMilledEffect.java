package com.github.laxika.magicalvibes.model.effect;

/** Mills each opponent and resolves the follow-up if at least one card was milled this way. */
public record MillEachOpponentThenIfMilledEffect(int count, CardEffect thenEffect)
        implements TriggeringSpellManaValueEffect {

    @Override
    public TargetSpec targetSpec() {
        return thenEffect.targetSpec();
    }

    @Override
    public CardEffect snapshotTriggeringSpellManaValue(int manaValue) {
        CardEffect snapshot = thenEffect instanceof TriggeringSpellManaValueEffect manaAware
                ? manaAware.snapshotTriggeringSpellManaValue(manaValue)
                : thenEffect;
        return new MillEachOpponentThenIfMilledEffect(count, snapshot);
    }
}
