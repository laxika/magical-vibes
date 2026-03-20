package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect (CR 614): "If [this creature] would die, instead exile it
 * with {@code count} egg counters on it."
 *
 * <p>Checked in {@code GraveyardService.addCardToGraveyard()} when the card would
 * be put into the graveyard from the battlefield (i.e. "die"). When matched, the
 * card is exiled and its egg counters are tracked in
 * {@code GameData.exiledCardEggCounters}.</p>
 *
 * <p>Pair with upkeep-trigger handling in {@code StepTriggerService} which removes
 * one egg counter per upkeep and returns the card to the battlefield when the last
 * counter is removed.</p>
 */
public record ExileWithEggCountersInsteadOfDyingEffect(int count) implements CardEffect {
}
