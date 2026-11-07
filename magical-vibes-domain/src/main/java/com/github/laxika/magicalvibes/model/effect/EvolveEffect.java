package com.github.laxika.magicalvibes.model.effect;

/**
 * The effect of the evolve keyword (CR 702.100a): "put a +1/+1 counter on this creature". The keyword's collector puts
 * it on the stack inside the ability's intervening "if"
 * ({@link com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource}),
 * which is checked as the ability triggers and again as it resolves (CR 603.4). The creature evolves when the counter
 * is put on it (CR 702.100b), which is what {@code Triggers.evolves()} watches; a card's own "whenever another creature
 * you control enters, if it has greater power or toughness than this creature, put a +1/+1 counter on this creature"
 * that isn't the keyword is that trigger, intervening "if" and {@code PutCountersOnSourceEffect}, and doesn't evolve.
 * Cards don't author this.
 */
public record EvolveEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
