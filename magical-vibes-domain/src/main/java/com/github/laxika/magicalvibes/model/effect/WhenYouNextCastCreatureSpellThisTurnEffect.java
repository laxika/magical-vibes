package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a delayed triggered ability: "When you next cast a creature spell this turn, {@code effect}."
 * The wrapped effect goes on the stack above that creature spell as a triggered ability and may
 * reference the spell via {@link TriggeringSpellReferencingEffect}. Tracked in
 * {@code GameData.nextCreatureSpellDelayedTriggersThisTurn}, fired by
 * {@code TriggerCollectionService.checkSpellCastTriggers} and cleared at end of turn.
 *
 * @param effect the effect the delayed trigger resolves
 */
public record WhenYouNextCastCreatureSpellThisTurnEffect(CardEffect effect) implements CardEffect {
}
