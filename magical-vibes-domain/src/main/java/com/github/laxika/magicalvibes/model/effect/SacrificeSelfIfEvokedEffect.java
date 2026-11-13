package com.github.laxika.magicalvibes.model.effect;

/**
 * Evoke's sacrifice (CR 702.74a): "When this permanent enters, if its evoke cost was paid, its controller sacrifices
 * it." Placed in a "when this enters" ability, it is judged as the ability triggers ({@link #asItTriggersOnEntering}):
 * its controller's sacrifice when the permanent was evoked, and the ability doesn't trigger otherwise (an intervening
 * "if", CR 603.4). It therefore never reaches stack resolution as itself.
 */
public record SacrificeSelfIfEvokedEffect() implements CardEffect {

    @Override
    public CardEffect asItTriggersOnEntering(EntryFacts facts) {
        return facts.evoked() ? new SacrificeSelfEffect(true) : null;
    }
}
