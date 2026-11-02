package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker: "Whenever you play a card with the same name as one of the cards exiled with this
 * permanent, …" (Search the City). Put it on <em>both</em> {@code Triggers.youCastSpell()} and
 * {@code Triggers.youPlayLand()} — "play a card" covers casting a spell and playing a land.
 *
 * <p>The trigger collectors ({@code SpellCastTriggerCollectorService} /
 * {@code MiscTriggerCollectorService}) do the name comparison and, on a match, push the concrete
 * ability: {@code MayEffect(PutCardExiledWithSourceIntoHandEffect(name))} followed by
 * {@code ConditionalEffect(NoCardsExiledWithSource, SequenceEffect(SacrificeSelfEffect,
 * ControllerExtraTurnEffect(1)))}.
 */
public record PlayedCardNameMatchesCardExiledWithSourceTriggerEffect() implements CardEffect {
}
