package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker stored in an Emblem's staticEffects list: "Whenever an opponent casts their first spell
 * each turn, counter that spell." Recognised as a capability interface in
 * {@code TriggerCollectionService} (so the concrete marker is not an effect-dispatch instanceof).
 *
 * <p>Per official rulings: triggers on each opponent's first spell on any turn (not only that
 * opponent's turn); if the counter fails (e.g. can't-be-countered), it still does not trigger again
 * for that player's later spells that turn; with multiple opponents, once per opponent per turn.
 */
public interface CounterOpponentFirstSpellEachTurnEffect extends CardEffect {

    /** Concrete instance placed on the emblem. */
    record Marker() implements CounterOpponentFirstSpellEachTurnEffect {
    }
}
