package com.github.laxika.magicalvibes.model.effect;

/**
 * Increment (CR keyword, Secrets of Strixhaven): "Whenever you cast a spell, if the amount of mana
 * you spent is greater than this creature's power or toughness, put a +1/+1 counter on this creature."
 * <p>
 * This is the resolution effect placed on the stack by the Increment trigger. The trigger itself is
 * driven by the Scryfall-loaded {@code INCREMENT} keyword (see
 * {@code TriggerCollectionService.collectIncrementTriggers}) rather than a per-card effect slot — cards
 * with the keyword get it automatically. The collector snapshots the mana spent into the stack entry's
 * {@code xValue} and performs the intervening-if check at trigger time; the resolution handler re-checks
 * the condition against the creature's current power/toughness (CR 603.4) before placing the counter.
 */
public record IncrementTriggerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
