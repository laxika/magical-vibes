package com.github.laxika.magicalvibes.model.effect;

/**
 * "When this creature enters, if tribute wasn't paid, [wrapped]" (CR 702.104b): an intervening "if" judged as the
 * ability triggers ({@link #asItTriggersOnEntering}), the wrapped effect when tribute wasn't paid; the ability doesn't
 * trigger when it was.
 */
public record TributeNotPaidEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }

    @Override
    public CardEffect asItTriggersOnEntering(EntryFacts facts) {
        return facts.tributePaid() ? null : wrapped;
    }
}
